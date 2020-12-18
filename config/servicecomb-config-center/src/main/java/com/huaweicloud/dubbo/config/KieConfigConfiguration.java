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
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_CONFIG_ADDRESSTYPE;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_APPLICATION;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_VERSION;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.servicecomb.config.kie.client.model.ConfigConstants;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;


public class KieConfigConfiguration {

  //初始化配置属性值，这里统一初始化，对于client模块，解耦对默认值的感知,屏蔽不同框架带来的配置项差异对KieAddressManager的影响
  public static KieAddressManager createKieAddressManager() {
    Properties properties = ConfigUtils.getProperties();
    Map<String, String> configKey = new HashMap<>();
    properties.setProperty(KEY_SERVICE_PROJECT,ConfigUtils.getProperty(KEY_SERVICE_PROJECT, "default"));
    properties.setProperty(KEY_CONFIG_ADDRESSTYPE,ConfigUtils.getProperty(KEY_CONFIG_ADDRESSTYPE, ""));
    properties.setProperty(KEY_SERVICE_ENABLELONGPOLLING,ConfigUtils.getProperty(KEY_SERVICE_ENABLELONGPOLLING, "true"));
    properties.setProperty(KEY_SERVICE_POLLINGWAITSEC,ConfigUtils.getProperty(KEY_SERVICE_POLLINGWAITSEC, "30"));
    String address = properties.getProperty(KEY_CONFIG_ADDRESS, "http://127.0.0.1:30110");
    configKey.put(ConfigConstants.KEY_PROJECT,KEY_SERVICE_PROJECT);
    configKey.put(ConfigConstants.KEY_ENABLELONGPOLLING,KEY_SERVICE_ENABLELONGPOLLING);
    configKey.put(ConfigConstants.KEY_POLLINGWAITSEC,KEY_SERVICE_POLLINGWAITSEC);
    return new KieAddressManager(properties, Arrays.asList(address.split(",")), configKey);
  }

  public static ConfigurationsRequest createConfigurationsRequest() {
    ConfigurationsRequest request = new ConfigurationsRequest();
    request.setApplication(ConfigUtils.getProperty(KEY_SERVICE_APPLICATION, "default"));
    request.setServiceName(ConfigUtils.getProperty(KEY_SERVICE_NAME, "defaultMicroserviceName"));
    request.setVersion(ConfigUtils.getProperty(KEY_SERVICE_VERSION, "1.0.0.0"));
    request.setEnvironment(ConfigUtils.getProperty(KEY_SERVICE_ENVIRONMENT, ""));
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }
}
