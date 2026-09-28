/*
 *
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class CommonConfigurationTest {

  @Test
  public void shouldDisableSslByDefault() {
    SSLProperties sslProperties = new CommonConfiguration(environment()).createSSLProperties();

    assertThat(sslProperties.isEnabled()).isFalse();
    assertThat(sslProperties.getSslOption()).isNull();
    assertThat(sslProperties.getSslCustom()).isNull();
  }

  @Test
  public void shouldUseSslDefaultsWhenEnabled() {
    StandardEnvironment environment = environment("dubbo.servicecomb.ssl.enabled", "true");

    SSLProperties sslProperties = new CommonConfiguration(environment).createSSLProperties();
    SSLOption sslOption = sslProperties.getSslOption();

    assertThat(sslProperties.isEnabled()).isTrue();
    assertThat(sslOption.getEngine()).isEqualTo("jdk");
    assertThat(sslOption.getProtocols()).isEqualTo("TLSv1.2");
    assertThat(sslOption.getCiphers()).isEqualTo(CommonConfiguration.DEFAULT_CIPHERS);
    assertThat(sslOption.isAuthPeer()).isFalse();
    assertThat(sslOption.getKeyStore()).isEqualTo("server.p12");
    assertThat(sslOption.getTrustStore()).isEqualTo("trust.jks");
  }

  @Test
  public void shouldMapCredentialPropertiesAndEncodeProject() {
    StandardEnvironment environment = environment(
        CommonConfiguration.KEY_AK_SK_ENABLED, "true",
        CommonConfiguration.KEY_AK_SK_ACCESS_KEY, "access-key",
        CommonConfiguration.KEY_AK_SK_SECRET_KEY, "secret-key",
        CommonConfiguration.KEY_AK_SK_CIPHER, DefaultCipher.CIPHER_NAME,
        CommonConfiguration.KEY_AK_SK_PROJECT, "project name/区域");

    AkSkRequestAuthHeaderProvider provider =
        new CommonConfiguration(environment).createAkSkRequestAuthHeaderProvider();

    assertThat(provider.getAccessKey()).isEqualTo("access-key");
    assertThat(provider.getCipher()).isEqualTo(DefaultCipher.CIPHER_NAME);
    assertThat(provider.getProject()).isEqualTo("project+name%2F%E5%8C%BA%E5%9F%9F");
    assertThat(provider.authHeaders()).hasSize(3);
  }

  @Test
  public void shouldKeepEmptyProjectUnchanged() {
    AkSkRequestAuthHeaderProvider provider =
        new CommonConfiguration(environment()).createAkSkRequestAuthHeaderProvider();

    assertThat(provider.getProject()).isEmpty();
  }

  private StandardEnvironment environment(String... keyValues) {
    Map<String, Object> properties = new HashMap<>();
    for (int i = 0; i < keyValues.length; i += 2) {
      properties.put(keyValues[i], keyValues[i + 1]);
    }
    StandardEnvironment environment = new StandardEnvironment();
    environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
    return environment;
  }
}
