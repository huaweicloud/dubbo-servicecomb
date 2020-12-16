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
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_VERSION;

import java.util.Arrays;

import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;


public class KieConfigConfiguration {

  public static KieAddressManager createKieAddressManager() {
    String address = ConfigUtils.getProperty(KEY_CONFIG_ADDRESS, "http://127.0.0.1:30103");
    String project = ConfigUtils.getProperty(KEY_SERVICE_PROJECT, "default");
    return new KieAddressManager(project, Arrays.asList(address.split(",")));
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
