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

import java.util.Arrays;

import org.apache.dubbo.common.utils.StringUtils;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.springframework.core.env.Environment;


public class KieConfigConfiguration {
  private Environment environment;

  public KieConfigConfiguration(Environment environment) {
    this.environment = environment;
  }

  //初始化配置属性值，这里统一初始化，对于client模块，解耦对默认值的感知,屏蔽不同框架带来的配置项差异对KieAddressManager的影响
  public KieAddressManager createKieAddressManager() {
    String address = environment.getProperty(KEY_CONFIG_ADDRESS, "");
    if (StringUtils.isEmpty(address)) {
      return null;
    }
    KieAddressManager kieAddressManager = new KieAddressManager(Arrays.asList(address.split(",")));
    return kieAddressManager;
  }


  public KieConfiguration createKieConfiguration() {
    KieConfiguration kieConfiguration = new KieConfiguration();
    kieConfiguration.setAppName(environment.getProperty(KEY_SERVICE_APPLICATION, "default"));
    kieConfiguration.setServiceName(environment.getProperty(KEY_SERVICE_NAME, "defaultMicroserviceName"));
    kieConfiguration.setEnvironment(environment.getProperty(KEY_SERVICE_ENVIRONMENT, ""));
    kieConfiguration.setProject(environment.getProperty(KEY_SERVICE_PROJECT, "default"));
    kieConfiguration.setCustomLabel(environment.getProperty(KEY_SERVICE_KIE_CUSTOMLABEL, "public"));
    kieConfiguration.setCustomLabelValue(environment.getProperty(KEY_SERVICE_KIE_CUSTOMLABELVALUE, ""));
    kieConfiguration
        .setEnableCustomConfig(Boolean.parseBoolean(environment.getProperty(KEY_SERVICE_KIE_ENABLECUSTOMCONFIG, "true")));
    kieConfiguration
        .setEnableServiceConfig(Boolean.parseBoolean(environment.getProperty(KEY_SERVICE_KIE_ENABLESERVICECONFIG, "true")));
    kieConfiguration
        .setEnableAppConfig(Boolean.parseBoolean(environment.getProperty(KEY_SERVICE_KIE_ENABLEAPPCONFIG, "true")));
    kieConfiguration
        .setFirstPullRequired(Boolean.parseBoolean(environment.getProperty(KEY_SERVICE_KIE_FRISTPULLREQUIRED, "true")));
    kieConfiguration
        .setEnableLongPolling(Boolean.parseBoolean(environment.getProperty(KEY_SERVICE_ENABLELONGPOLLING, "false")));
    kieConfiguration
        .setPollingWaitInSeconds(Integer.parseInt(environment.getProperty(KEY_SERVICE_POLLINGWAITSEC, "10")));
    return kieConfiguration;
  }
}
