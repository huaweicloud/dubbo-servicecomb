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

package com.huaweicloud.dubbo.config;

import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_CONFIG_ADDRESS;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_APPLICATION;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class ConfigCenterConfigurationTest {

  @Test
  public void shouldNotCreateAddressManagerForMissingAddress() {
    ConfigCenterConfiguration configuration = new ConfigCenterConfiguration(environment());

    assertThat(configuration.createAddressManager()).isNull();
  }

  @Test
  public void shouldCreateAddressManagerFromConfiguredAddress() {
    StandardEnvironment environment = environment(
        KEY_CONFIG_ADDRESS, "https://config.example.com:30103",
        KEY_SERVICE_PROJECT, "production");

    AddressManager addressManager = new ConfigCenterConfiguration(environment).createAddressManager();

    assertThat(addressManager).isNotNull();
    assertThat(addressManager.address()).isEqualTo("https://config.example.com:30103/v3/production");
    assertThat(addressManager.sslEnabled()).isTrue();
  }

  @Test
  public void shouldUseQueryDefaultsAndNullRevision() {
    QueryConfigurationsRequest request =
        new ConfigCenterConfiguration(environment()).createQueryConfigurationsRequest();

    assertThat(request.getApplication()).isEqualTo("default");
    assertThat(request.getServiceName()).isEqualTo("defaultMicroserviceName");
    assertThat(request.getVersion()).isEqualTo("1.0.0.0");
    assertThat(request.getEnvironment()).isEmpty();
    assertThat(request.getRevision()).isNull();
  }

  @Test
  public void shouldMapCustomQueryProperties() {
    StandardEnvironment environment = environment(
        KEY_SERVICE_APPLICATION, "application",
        KEY_SERVICE_NAME, "service",
        KEY_SERVICE_VERSION, "2.1.0",
        KEY_SERVICE_ENVIRONMENT, "production");

    QueryConfigurationsRequest request =
        new ConfigCenterConfiguration(environment).createQueryConfigurationsRequest();

    assertThat(request.getApplication()).isEqualTo("application");
    assertThat(request.getServiceName()).isEqualTo("service");
    assertThat(request.getVersion()).isEqualTo("2.1.0");
    assertThat(request.getEnvironment()).isEqualTo("production");
    assertThat(request.getRevision()).isNull();
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
