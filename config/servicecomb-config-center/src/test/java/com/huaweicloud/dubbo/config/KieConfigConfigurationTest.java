/*
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
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABEL;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABELVALUE;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_ENABLEAPPCONFIG;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_ENABLESERVICECONFIG;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_KIE_FRISTPULLREQUIRED;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class KieConfigConfigurationTest {

  @Test
  public void shouldNotCreateAddressManagerForMissingAddress() {
    KieConfigConfiguration configuration = new KieConfigConfiguration(environment());

    assertThat(configuration.createKieAddressManager()).isNull();
  }

  @Test
  public void shouldCreateSecureAddressManager() {
    StandardEnvironment environment = environment(KEY_CONFIG_ADDRESS, "https://kie.example.com:30110");

    KieAddressManager addressManager = new KieConfigConfiguration(environment).createKieAddressManager();

    assertThat(addressManager).isNotNull();
    assertThat(addressManager.address()).isEqualTo("https://kie.example.com:30110");
    assertThat(addressManager.sslEnabled()).isTrue();
  }

  @Test
  public void shouldUseKieDefaults() {
    KieConfiguration configuration =
        new KieConfigConfiguration(environment()).createKieConfiguration();

    assertThat(configuration.getAppName()).isEqualTo("default");
    assertThat(configuration.getServiceName()).isEqualTo("defaultMicroserviceName");
    assertThat(configuration.getEnvironment()).isEmpty();
    assertThat(configuration.getProject()).isEqualTo("default");
    assertThat(configuration.getCustomLabel()).isEqualTo("public");
    assertThat(configuration.getCustomLabelValue()).isEmpty();
    assertThat(configuration.isEnableCustomConfig()).isTrue();
    assertThat(configuration.isEnableServiceConfig()).isTrue();
    assertThat(configuration.isEnableAppConfig()).isTrue();
    assertThat(configuration.isFirstPullRequired()).isTrue();
    assertThat(configuration.isEnableLongPolling()).isFalse();
    assertThat(configuration.getPollingWaitInSeconds()).isEqualTo(10);
  }

  @Test
  public void shouldMapCustomKieProperties() {
    StandardEnvironment environment = environment(
        KEY_SERVICE_APPLICATION, "application",
        KEY_SERVICE_NAME, "service",
        KEY_SERVICE_ENVIRONMENT, "production",
        KEY_SERVICE_PROJECT, "project",
        KEY_SERVICE_KIE_CUSTOMLABEL, "region",
        KEY_SERVICE_KIE_CUSTOMLABELVALUE, "cn-north-4",
        KEY_SERVICE_KIE_ENABLECUSTOMCONFIG, "false",
        KEY_SERVICE_KIE_ENABLESERVICECONFIG, "false",
        KEY_SERVICE_KIE_ENABLEAPPCONFIG, "false",
        KEY_SERVICE_KIE_FRISTPULLREQUIRED, "false",
        KEY_SERVICE_ENABLELONGPOLLING, "true",
        KEY_SERVICE_POLLINGWAITSEC, "30");

    KieConfiguration configuration = new KieConfigConfiguration(environment).createKieConfiguration();

    assertThat(configuration.getAppName()).isEqualTo("application");
    assertThat(configuration.getServiceName()).isEqualTo("service");
    assertThat(configuration.getEnvironment()).isEqualTo("production");
    assertThat(configuration.getProject()).isEqualTo("project");
    assertThat(configuration.getCustomLabel()).isEqualTo("region");
    assertThat(configuration.getCustomLabelValue()).isEqualTo("cn-north-4");
    assertThat(configuration.isEnableCustomConfig()).isFalse();
    assertThat(configuration.isEnableServiceConfig()).isFalse();
    assertThat(configuration.isEnableAppConfig()).isFalse();
    assertThat(configuration.isFirstPullRequired()).isFalse();
    assertThat(configuration.isEnableLongPolling()).isTrue();
    assertThat(configuration.getPollingWaitInSeconds()).isEqualTo(30);
  }

  @Test
  public void shouldRejectNonNumericPollingWait() {
    StandardEnvironment environment = environment(KEY_SERVICE_POLLINGWAITSEC, "not-a-number");

    assertThatThrownBy(() -> new KieConfigConfiguration(environment).createKieConfiguration())
        .isInstanceOf(NumberFormatException.class);
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
