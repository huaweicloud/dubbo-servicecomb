/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.dubbo.common;

import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_CONFIG_ADDRESSTYPE;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_RBAC_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_RBAC_PASSWORD;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_REGISTRY_ADDRESS;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import javax.ws.rs.core.Response;

import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class RBACRequestAuthHeaderProvider implements AuthHeaderProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(RBACRequestAuthHeaderProvider.class);

  public static final String INVALID_TOKEN = "invalid";

  public static final String CACHE_KEY = "token";

  public static final String AUTH_HEADER = "Authorization";

  private static final long TOKEN_REFRESH_TIME_IN_SECONDS = 20 * 60 * 1000;

  private ExecutorService executorService;

  private LoadingCache<String, String> cache;

  private CommonConfiguration commonConfiguration;

  private Environment environment;

  public RBACRequestAuthHeaderProvider(CommonConfiguration commonConfiguration, Environment environment) {
    this.commonConfiguration = commonConfiguration;
    this.environment = environment;
    if (enabled()) {
      executorService = Executors.newFixedThreadPool(1, t -> new Thread(t, "rbac-executor"));
      cache = CacheBuilder.newBuilder().maximumSize(1).refreshAfterWrite(refreshTime(), TimeUnit.MILLISECONDS)
          .build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
              return createHeaders();
            }

            @Override
            public ListenableFuture<String> reload(String key, String oldValue) throws Exception {
              return Futures.submit(() -> createHeaders(), executorService);
            }
          });
    }
  }

  private boolean enabled() {
    return !StringUtils.isEmpty(ConfigUtils.getProperty(KEY_RBAC_NAME)) && !StringUtils
        .isEmpty(ConfigUtils.getProperty(KEY_RBAC_PASSWORD));
  }

  protected long refreshTime() {
    return TOKEN_REFRESH_TIME_IN_SECONDS;
  }

  protected String createHeaders() {
    LOGGER.info("start to create RBAC headers");
    RbacTokenResponse rbacTokenResponse = callCreateHeaders();
    int statusCode = rbacTokenResponse.getStatusCode();
    if (Response.Status.UNAUTHORIZED.getStatusCode() == statusCode
        || Response.Status.FORBIDDEN.getStatusCode() == statusCode) {
      // password wrong, do not try anymore
      LOGGER.warn("username or password may be wrong, stop trying to query tokens.");
      return INVALID_TOKEN;
    } else if (Response.Status.NOT_FOUND.getStatusCode() == statusCode) {
      // service center not support, do not try
      LOGGER.warn("service center do not support RBAC token, you should not config account info");
      return INVALID_TOKEN;
    }
    LOGGER.info("refresh token successfully {}", statusCode);
    return rbacTokenResponse.getToken();
  }

  protected RbacTokenResponse callCreateHeaders() {
    List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
    ServiceCenterClient serviceCenterClient = serviceCenterClient(authHeaderProviders);
    RbacTokenRequest request = new RbacTokenRequest();
    request.setName(ConfigUtils.getProperty(KEY_RBAC_NAME));
    request.setPassword(ConfigUtils.getProperty(KEY_RBAC_PASSWORD));
    return serviceCenterClient.queryToken(request);
  }

  public static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(authHeaderProvider -> headers.putAll(authHeaderProvider.authHeaders()));
      return headers;
    };
  }

  public ServiceCenterClient serviceCenterClient(List<AuthHeaderProvider> authHeaderProviders) {
    SSLProperties sslProperties = commonConfiguration.createSSLProperties();
    AddressManager addressManager = createAddressManager();
    return new ServiceCenterClient(addressManager, sslProperties, getRequestAuthHeaderProvider(authHeaderProviders),
        "default", new HashMap<>());
  }

  @Override
  public Map<String, String> authHeaders() {
    if (!enabled()) {
      return Collections.emptyMap();
    }
    try {
      String header = cache.get(CACHE_KEY);
      if (!StringUtils.isEmpty(header)) {
        Map<String, String> tokens = new HashMap<>(1);
        tokens.put(AUTH_HEADER, "Bearer " + header);
        return tokens;
      }
    } catch (Exception e) {
      LOGGER.error("Get auth headers failed", e);
    }
    return Collections.emptyMap();
  }

  public AddressManager createAddressManager() {
    String address = environment.getProperty(KEY_REGISTRY_ADDRESS, "http://127.0.0.1:30100");
    String project = environment.getProperty(KEY_SERVICE_PROJECT, "default");
    String type = environment.getProperty(KEY_CONFIG_ADDRESSTYPE, "");
    LOGGER.info("initialize config server type={}, addresss={}.", type, address);
    return new AddressManager(project, Arrays.asList(address.split(",")));
  }

}
