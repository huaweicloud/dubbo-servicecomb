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

package com.huaweicloud.dubbo.discovery;

import java.util.Arrays;

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration.AKSKProperties;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;

public class Configuration {
  public static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
      + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  public static final String KEY_SERVICE_APPLICATION = "dubbo.servicecomb.service.application";

  public static final String KEY_SERVICE_NAME = "dubbo.servicecomb.service.name";

  public static final String KEY_SERVICE_VERSION = "dubbo.servicecomb.service.version";

  public static final String KEY_SERVICE_ENVIRONMENT = "dubbo.servicecomb.service.environment";

  public static final String KEY_INSTANCE_ENVIRONMENT = "dubbo.servicecomb.instance.initialStatus";

  public static final String KEY_REGISTRY_ADDRESS = "dubbo.servicecomb.registry.address";

  public static final String KEY_REGISTRY_PROJECT = "dubbo.servicecomb.registry.project";

  public static final String KEY_REGISTRY_SSL_ENABLED = "dubbo.servicecomb.registry.ssl.enabled";

  public static final String KEY_REGISTRY_SSL_ENGINE = "dubbo.servicecomb.registry.ssl.engine";

  public static final String KEY_REGISTRY_SSL_PROTOCOLS = "dubbo.servicecomb.registry.ssl.protocols";

  public static final String KEY_REGISTRY_SSL_CIPHERS = "dubbo.servicecomb.registry.ssl.ciphers";

  public static final String KEY_REGISTRY_SSL_AUTH_PEER = "dubbo.servicecomb.registry.ssl.authPeer";

  public static final String KEY_REGISTRY_SSL_CHECKCN_HOST = "dubbo.servicecomb.registry.ssl.checkCNHost";

  public static final String KEY_REGISTRY_SSL_CHECKCN_WHITE = "dubbo.servicecomb.registry.ssl.checkCNWhite";

  public static final String KEY_REGISTRY_SSL_CHECKCN_WHITE_FILE = "dubbo.servicecomb.registry.ssl.checkCNWhiteFile";

  public static final String KEY_REGISTRY_SSL_ALLOW_RENEGOTIATE = "dubbo.servicecomb.registry.ssl.allowRenegotiate";

  public static final String KEY_REGISTRY_SSL_STORE_PATH = "dubbo.servicecomb.registry.ssl.storePath";

  public static final String KEY_REGISTRY_SSL_TRUST_STORE = "dubbo.servicecomb.registry.ssl.trustStore";

  public static final String KEY_REGISTRY_SSL_TRUST_STORE_TYPE = "dubbo.servicecomb.registry.ssl.trustStoreType";

  public static final String KEY_REGISTRY_SSL_TRUST_STORE_VALUE = "dubbo.servicecomb.registry.ssl.trustStoreValue";

  public static final String KEY_REGISTRY_SSL_KEYSTORE = "dubbo.servicecomb.registry.ssl.keyStore";

  public static final String KEY_REGISTRY_SSL_KEYSTORE_TYPE = "dubbo.servicecomb.registry.ssl.keyStoreType";

  public static final String KEY_REGISTRY_SSL_KEYSTORE_VALUE = "dubbo.servicecomb.registry.ssl.keyStoreValue";

  public static final String KEY_REGISTRY_SSL_CRL = "dubbo.servicecomb.registry.ssl.crl";

  public static final String KEY_REGISTRY_SSL_SSL_CUSTOM_CLASS = "dubbo.servicecomb.registry.ssl.sslCustomClass";

  public static final String KEY_REGISTRY_AK_SK_ENABLED = "dubbo.servicecomb.registry.credentials.enabled";

  public static final String KEY_REGISTRY_AK_SK_ACCESS_KEY = "dubbo.servicecomb.registry.credentials.accessKey";

  public static final String KEY_REGISTRY_AK_SK_SECRET_KEY = "dubbo.servicecomb.registry.credentials.secretKey";

  public static final String KEY_REGISTRY_AK_SK_CIPHER = "dubbo.servicecomb.registry.credentials.cipher";

  public static final String KEY_REGISTRY_AK_SK_PROJECT = "dubbo.servicecomb.registry.credentials.project";

  public static Microservice createMicroservice() {
    Microservice microservice = new Microservice();
    microservice.setAppId(ConfigUtils.getProperty(KEY_SERVICE_APPLICATION, "default"));
    microservice.setServiceName(ConfigUtils.getProperty(KEY_SERVICE_NAME, "defaultMicroserviceName"));
    microservice.setVersion(ConfigUtils.getProperty(KEY_SERVICE_VERSION, "1.0.0.0"));
    microservice.setEnvironment(ConfigUtils.getProperty(KEY_SERVICE_ENVIRONMENT, ""));
    Framework framework = new Framework();
    framework.setName("DUBBO");
    framework.setVersion(AbstractRegistryFactory.class.getPackage().getImplementationVersion());
    microservice.setFramework(framework);
    return microservice;
  }

  public static MicroserviceInstance createMicroserviceInstance() {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setStatus(MicroserviceInstanceStatus.valueOf(ConfigUtils.getProperty(KEY_INSTANCE_ENVIRONMENT, "UP")));
    return instance;
  }

  public static AddressManager createAddressManager() {
    String address = ConfigUtils.getProperty(KEY_REGISTRY_ADDRESS, "http://127.0.0.1:30100");
    String project = ConfigUtils.getProperty(KEY_REGISTRY_PROJECT, "default");
    return new AddressManager(project, Arrays.asList(address.split(",")));
  }

  public static SSLProperties createSSLProperties() {
    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setEnabled(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_SSL_ENABLED, "false")));
    if (sslProperties.isEnabled()) {
      SSLOption option = new SSLOption();
      option.setEngine(ConfigUtils.getProperty(KEY_REGISTRY_SSL_ENGINE, "jdk"));
      option.setProtocols(ConfigUtils.getProperty(KEY_REGISTRY_SSL_PROTOCOLS, "TLSv1.2"));
      option.setCiphers(ConfigUtils.getProperty(KEY_REGISTRY_SSL_CIPHERS, DEFAULT_CIPHERS));
      option.setAuthPeer(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_SSL_AUTH_PEER, "false")));
      option.setCheckCNHost(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_SSL_CHECKCN_HOST, "false")));
      option.setCheckCNWhite(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_SSL_CHECKCN_WHITE, "false")));
      option.setCheckCNWhiteFile(ConfigUtils.getProperty(KEY_REGISTRY_SSL_CHECKCN_WHITE_FILE, "white.list"));
      option.setAllowRenegociate(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_SSL_ALLOW_RENEGOTIATE, "false")));
      option.setStorePath(ConfigUtils.getProperty(KEY_REGISTRY_SSL_STORE_PATH, "internal"));
      option.setKeyStore(ConfigUtils.getProperty(KEY_REGISTRY_SSL_KEYSTORE, "server.p12"));
      option.setKeyStoreType(ConfigUtils.getProperty(KEY_REGISTRY_SSL_KEYSTORE_TYPE, "PKCS12"));
      option.setKeyStoreValue(ConfigUtils.getProperty(KEY_REGISTRY_SSL_KEYSTORE_VALUE, "keyStoreValue"));
      option.setTrustStore(ConfigUtils.getProperty(KEY_REGISTRY_SSL_TRUST_STORE, "trust.jks"));
      option.setTrustStoreType(ConfigUtils.getProperty(KEY_REGISTRY_SSL_TRUST_STORE_TYPE, "JKS"));
      option.setTrustStoreValue(ConfigUtils.getProperty(KEY_REGISTRY_SSL_TRUST_STORE_VALUE, "trustStoreValue"));
      option.setCrl(ConfigUtils.getProperty(KEY_REGISTRY_SSL_CRL, "revoke.crl"));

      SSLCustom sslCustom = SSLCustom.createSSLCustom(ConfigUtils.getProperty(KEY_REGISTRY_SSL_SSL_CUSTOM_CLASS, null));
      sslProperties.setSslOption(option);
      sslProperties.setSslCustom(sslCustom);
    }
    return sslProperties;
  }

  public static AKSKProperties createAKSKProperties() {
    AKSKProperties akskProperties = new AKSKProperties();
    akskProperties.setEnabled(Boolean.valueOf(ConfigUtils.getProperty(KEY_REGISTRY_AK_SK_ENABLED, "false")));
    akskProperties.setAccessKey(ConfigUtils.getProperty(KEY_REGISTRY_AK_SK_ACCESS_KEY, null));
    akskProperties.setSecretKey(ConfigUtils.getProperty(KEY_REGISTRY_AK_SK_SECRET_KEY, null));
    akskProperties.setCipher(ConfigUtils.getProperty(KEY_REGISTRY_AK_SK_CIPHER, null));
    akskProperties.setProject(ConfigUtils.getProperty(KEY_REGISTRY_AK_SK_PROJECT, null));
    return akskProperties;
  }
}
