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

package org.apache.servicecomb.config.kie.client;

import com.huaweicloud.dubbo.common.CommonConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.kie.client.exception.OperationException;
import org.apache.servicecomb.config.kie.client.model.*;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KieClient implements KieConfigOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

  private AtomicBoolean isFirst = new AtomicBoolean(true);

  protected HttpTransport httpTransport;

  protected String revision = "0";

  private String url;

  private HttpResponse httpResponse = null;

  private KieAddressManager addressManager;

  private Map<String, String> labelsMap;

  public static final String DEFAULT_KIE_API_VERSION = "v1";

  public KieClient(KieAddressManager addressManager, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    this.addressManager = addressManager;
  }

  @Override
  public ConfigurationsResponse queryConfigurations(ConfigurationsRequest request) {
    boolean isWatch = false;
    String enableLongPolling = ConfigUtils.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING, "false");
    if (enableLongPolling.equals("true")) {
      isWatch = true;
    }
    try {
      url = addressManager.getUrl()
          + "/"
          + DEFAULT_KIE_API_VERSION
          + "/"
          + addressManager.getProjectName()
          + "/kie/kv?label=app:"
          //app 名称作为筛选条件
          + request.getApplication()
          + "&revision="
          + revision;

      if (isWatch && !isFirst.get()) {
        url +=
            "&wait=" + ConfigUtils.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC, "30") + "s";
      }
      isFirst.compareAndSet(true, false);

      Map<String, String> headers = new HashMap<>();
      headers.put("environment", request.getEnvironment());
      HttpRequest httpRequest = new HttpRequest(url, headers, null, HttpRequest.GET);
      httpResponse = httpTransport.doRequest(httpRequest);
      if (httpResponse == null) {
        return null;
      }
      ConfigurationsResponse configurationsResponse = new ConfigurationsResponse();
      if (httpResponse.getStatusCode() == HttpStatus.SC_OK) {
        revision = httpResponse.getHeader("X-Kie-Revision");
        KVResponse allConfigList = JsonUtils.OBJ_MAPPER
            .readValue(httpResponse.getContent(), KVResponse.class);
        Map<String, Object> Configurations = getConfigByLabel(allConfigList);
        configurationsResponse.setConfigurations(Configurations);
        configurationsResponse.setChanged(true);
        configurationsResponse.setRevision(revision);
        return configurationsResponse;
      }
      if (httpResponse.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        throw new OperationException("Bad request for query configurations.");
      }
      if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        configurationsResponse.setChanged(false);
        return configurationsResponse;
      }
      throw new OperationException(
          "read response failed. status:" + httpResponse.getStatusCode() + "; message:" +
              httpResponse.getMessage() + "; content:" + httpResponse.getContent());

    } catch (Exception e) {
      addressManager.toggle();
      throw new OperationException("read response failed. " + httpResponse, e);
    }
  }

  private Map<String, Object> getConfigByLabel(KVResponse resp) {
    Map<String, Object> resultMap = new HashMap<>();
    List<KVDoc> appList = new ArrayList<>();
    List<KVDoc> serviceList = new ArrayList<>();
    List<KVDoc> versionList = new ArrayList<>();
    for (KVDoc kvDoc : resp.getData()) {
      if (!StringUtils.isEmpty(kvDoc.getStatus()) && !kvDoc.getStatus()
          .equals(ConfigConstants.STATUS_ENABLED)) {
        continue;
      }
      labelsMap = kvDoc.getLabels();
      boolean checkApplication = checkValue(ConfigConstants.LABEL_APP, CommonConfiguration.KEY_SERVICE_APPLICATION,
          "default");
      boolean checkEnvironment = checkValue(ConfigConstants.LABEL_ENV, CommonConfiguration.KEY_SERVICE_ENVIRONMENT,
          "");
      boolean checkServer = checkValue(ConfigConstants.LABEL_SERVICE, CommonConfiguration.KEY_SERVICE_NAME,
          "");
      boolean checkVersion = checkValue(ConfigConstants.LABEL_VERSION, CommonConfiguration.KEY_SERVICE_VERSION,
          "1.0.0");
      if (checkApplication && checkEnvironment && !labelsMap.containsKey(ConfigConstants.LABEL_SERVICE)) {
          appList.add(kvDoc);
      }
      if (checkApplication && checkEnvironment && checkServer && !kvDoc.getLabels().containsKey(ConfigConstants.LABEL_VERSION)) {
        serviceList.add(kvDoc);
      }
      if (checkApplication && checkEnvironment && checkServer && checkVersion) {
        versionList.add(kvDoc);
      }
    }
    //kv is priority
    for (KVDoc kvDoc : appList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : serviceList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : versionList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    return resultMap;
  }

  private boolean checkValue(String key, String propertyName, String defaultValue) {
    if (!labelsMap.containsKey(key)) {
      return false;
    }
    if (!labelsMap.get(key).equals(ConfigUtils.getProperty(propertyName, defaultValue))) {
      return false;
    }
    return true;
  }

  private Map<String, Object> processValueType(KVDoc kvDoc) {
    ValueType vtype;
    try {
      vtype = ValueType.valueOf(kvDoc.getValueType());
    } catch (IllegalArgumentException e) {
      throw new OperationException("value type not support");
    }
    Properties properties = new Properties();
    Map<String, Object> kvMap = new HashMap<>();
    try {
      switch (vtype) {
        case yml:
        case yaml:
          YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
          yamlFactory.setResources(new ByteArrayResource(kvDoc.getValue().getBytes()));
          return toMap(kvDoc.getKey(), yamlFactory.getObject());
        case properties:
          properties.load(new StringReader(kvDoc.getValue()));
          return toMap(kvDoc.getKey(), properties);
        case text:
        case string:
        default:
          kvMap.put(kvDoc.getKey(), kvDoc.getValue());
          return kvMap;
      }
    } catch (Exception e) {
      LOGGER.error("read config failed");
    }
    return Collections.emptyMap();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMap(String prefix, Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      if (!StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }
      if (value != null) {
        result.put(key, value);
      } else {
        result.put(key, null);
      }
    }
    return result;
  }
}
