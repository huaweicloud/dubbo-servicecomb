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

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CommonConfiguration {
  public static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
      + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  // ###### service configuration ############### //
  public static final String KEY_SERVICE_PROJECT = "dubbo.servicecomb.service.project";

  public static final String KEY_SERVICE_APPLICATION = "dubbo.servicecomb.service.application";

  public static final String KEY_SERVICE_NAME = "dubbo.servicecomb.service.name";

  public static final String KEY_SERVICE_VERSION = "dubbo.servicecomb.service.version";

  public static final String KEY_SERVICE_ENVIRONMENT = "dubbo.servicecomb.service.environment";

  // ###### service instance configuration ############### //
  public static final String KEY_INSTANCE_ENVIRONMENT = "dubbo.servicecomb.instance.initialStatus";

  // ###### service center configuration ############### //
  public static final String KEY_REGISTRY_ADDRESS = "dubbo.servicecomb.registry.address";

  // ###### config center configuration ############### //
  public static final String KEY_CONFIG_ADDRESSTYPE = "dubbo.servicecomb.config.type";

  public static final String KEY_CONFIG_FILESOURCE = "dubbo.servicecomb.config.fileSource";

  public static final String KEY_CONFIG_ADDRESS = "dubbo.servicecomb.config.address";

  // ###### kie config center polling configuration############### //
  public static final String KEY_SERVICE_ENABLELONGPOLLING = "dubbo.servicecomb.config.enableLongPolling";

  public static final String KEY_SERVICE_POLLINGWAITSEC = "dubbo.servicecomb.config.pollingWaitInSeconds";

  // ###### ssl configuration ############### //
  public static final String KEY_SSL_ENABLED = "dubbo.servicecomb.ssl.enabled";

  public static final String KEY_SSL_ENGINE = "dubbo.servicecomb.ssl.engine";

  public static final String KEY_SSL_PROTOCOLS = "dubbo.servicecomb.ssl.protocols";

  public static final String KEY_SSL_CIPHERS = "dubbo.servicecomb.ssl.ciphers";

  public static final String KEY_SSL_AUTH_PEER = "dubbo.servicecomb.ssl.authPeer";

  public static final String KEY_SSL_CHECKCN_HOST = "dubbo.servicecomb.ssl.checkCNHost";

  public static final String KEY_SSL_CHECKCN_WHITE = "dubbo.servicecomb.ssl.checkCNWhite";

  public static final String KEY_SSL_CHECKCN_WHITE_FILE = "dubbo.servicecomb.ssl.checkCNWhiteFile";

  public static final String KEY_SSL_ALLOW_RENEGOTIATE = "dubbo.servicecomb.ssl.allowRenegotiate";

  public static final String KEY_SSL_STORE_PATH = "dubbo.servicecomb.ssl.storePath";

  public static final String KEY_SSL_TRUST_STORE = "dubbo.servicecomb.ssl.trustStore";

  public static final String KEY_SSL_TRUST_STORE_TYPE = "dubbo.servicecomb.ssl.trustStoreType";

  public static final String KEY_SSL_TRUST_STORE_VALUE = "dubbo.servicecomb.ssl.trustStoreValue";

  public static final String KEY_SSL_KEYSTORE = "dubbo.servicecomb.ssl.keyStore";

  public static final String KEY_SSL_KEYSTORE_TYPE = "dubbo.servicecomb.ssl.keyStoreType";

  public static final String KEY_SSL_KEYSTORE_VALUE = "dubbo.servicecomb.ssl.keyStoreValue";

  public static final String KEY_SSL_CRL = "dubbo.servicecomb.ssl.crl";

  public static final String KEY_SSL_SSL_CUSTOM_CLASS = "dubbo.servicecomb.ssl.sslCustomClass";

  // ###### ak / ak configuration ############### //
  public static final String KEY_AK_SK_ENABLED = "dubbo.servicecomb.credentials.enabled";

  public static final String KEY_AK_SK_ACCESS_KEY = "dubbo.servicecomb.credentials.accessKey";

  public static final String KEY_AK_SK_SECRET_KEY = "dubbo.servicecomb.credentials.secretKey";

  public static final String KEY_AK_SK_CIPHER = "dubbo.servicecomb.credentials.cipher";

  public static final String KEY_AK_SK_PROJECT = "dubbo.servicecomb.credentials.project";

  private Environment environment;

  public CommonConfiguration(Environment environment) {
    this.environment = environment;
  }

  public SSLProperties createSSLProperties() {
    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setEnabled(Boolean.valueOf(environment.getProperty(KEY_SSL_ENABLED, "false")));
    if (sslProperties.isEnabled()) {
      SSLOption option = new SSLOption();
      option.setEngine(environment.getProperty(KEY_SSL_ENGINE, "jdk"));
      option.setProtocols(environment.getProperty(KEY_SSL_PROTOCOLS, "TLSv1.2"));
      option.setCiphers(environment.getProperty(KEY_SSL_CIPHERS, DEFAULT_CIPHERS));
      option.setAuthPeer(Boolean.valueOf(environment.getProperty(KEY_SSL_AUTH_PEER, "false")));
      option.setCheckCNHost(Boolean.valueOf(environment.getProperty(KEY_SSL_CHECKCN_HOST, "false")));
      option.setCheckCNWhite(Boolean.valueOf(environment.getProperty(KEY_SSL_CHECKCN_WHITE, "false")));
      option.setCheckCNWhiteFile(environment.getProperty(KEY_SSL_CHECKCN_WHITE_FILE, "white.list"));
      option.setAllowRenegociate(Boolean.valueOf(environment.getProperty(KEY_SSL_ALLOW_RENEGOTIATE, "false")));
      option.setStorePath(environment.getProperty(KEY_SSL_STORE_PATH, "internal"));
      option.setKeyStore(environment.getProperty(KEY_SSL_KEYSTORE, "server.p12"));
      option.setKeyStoreType(environment.getProperty(KEY_SSL_KEYSTORE_TYPE, "PKCS12"));
      option.setKeyStoreValue(environment.getProperty(KEY_SSL_KEYSTORE_VALUE, "keyStoreValue"));
      option.setTrustStore(environment.getProperty(KEY_SSL_TRUST_STORE, "trust.jks"));
      option.setTrustStoreType(environment.getProperty(KEY_SSL_TRUST_STORE_TYPE, "JKS"));
      option.setTrustStoreValue(environment.getProperty(KEY_SSL_TRUST_STORE_VALUE, "trustStoreValue"));
      option.setCrl(environment.getProperty(KEY_SSL_CRL, "revoke.crl"));

      SSLCustom sslCustom = SSLCustom.createSSLCustom(environment.getProperty(KEY_SSL_SSL_CUSTOM_CLASS, ""));
      sslProperties.setSslOption(option);
      sslProperties.setSslCustom(sslCustom);
    }
    return sslProperties;
  }

  public RequestAuthHeaderProvider createRequestAuthHeaderProvider() {
    DubboRequestAuthHeaderProvider requestAuthHeaderProvider = new DubboRequestAuthHeaderProvider();
    requestAuthHeaderProvider.setEnabled(Boolean.valueOf(environment.getProperty(KEY_AK_SK_ENABLED, "false")));
    requestAuthHeaderProvider.setAccessKey(environment.getProperty(KEY_AK_SK_ACCESS_KEY, ""));
    requestAuthHeaderProvider.setSecretKey(environment.getProperty(KEY_AK_SK_SECRET_KEY, ""));
    requestAuthHeaderProvider.setCipher(environment.getProperty(KEY_AK_SK_CIPHER, ""));
    requestAuthHeaderProvider.setProject(safeGetProject(environment.getProperty(KEY_AK_SK_PROJECT, "")));
    return requestAuthHeaderProvider;
  }

  private String safeGetProject(String project) {
    if (StringUtils.isEmpty(project)) {
      return project;
    }
    try {
      return URLEncoder.encode(project, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return project;
    }
  }
}
