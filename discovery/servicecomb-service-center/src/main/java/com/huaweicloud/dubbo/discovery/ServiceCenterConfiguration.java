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

import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_INSTANCE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_REGISTRY_ADDRESS;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_APPLICATION;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_NAME;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_PROJECT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_SERVICE_VERSION;

import java.util.Arrays;

import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;

public class ServiceCenterConfiguration {
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
    String project = ConfigUtils.getProperty(KEY_SERVICE_PROJECT, "default");
    return new AddressManager(project, Arrays.asList(address.split(",")));
  }
}
