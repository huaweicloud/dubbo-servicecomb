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

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;

import com.alibaba.dubbo.common.utils.ConfigUtils;

public class Configuration {
  public static final String KEY_CREDENTIALS_ACCESS_KEY = "dubbo.servicecomb.credentials.accessKey";

  public static final String KEY_CREDENTIALS_SECRETE_KEY = "dubbo.servicecomb.credentials.secretKey";

  public static final String KEY_CREDENTIALS_PROJECT = "dubbo.servicecomb.credentials.project";

  public static final String KEY_SERVICE_APPLICATION = "dubbo.servicecomb.service.application";

  public static final String KEY_SERVICE_NAME = "dubbo.servicecomb.service.name";

  public static final String KEY_SERVICE_VERSION = "dubbo.servicecomb.service.version";

  public static final String KEY_SERVICE_ENVIRONMENT = "dubbo.servicecomb.service.environment";

  public static final String KEY_INSTANCE_ENVIRONMENT = "dubbo.servicecomb.instance.initialStatus";

  public static final String KEY_REGISTRY_ADDRESS = "dubbo.servicecomb.registry.address";

  public static Microservice createMicroservice() {
    Microservice microservice = new Microservice();
    microservice.setAppId(ConfigUtils.getProperty(KEY_SERVICE_APPLICATION, "default"));
    microservice.setServiceName(ConfigUtils.getProperty(KEY_SERVICE_NAME, "defaultMicroserviceName"));
    microservice.setVersion(ConfigUtils.getProperty(KEY_SERVICE_VERSION, "1.0.0.0"));
    microservice.setEnvironment(ConfigUtils.getProperty(KEY_SERVICE_ENVIRONMENT, ""));
    return microservice;
  }

  public static MicroserviceInstance createMicroserviceInstance() {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setStatus(MicroserviceInstanceStatus.valueOf(ConfigUtils.getProperty(KEY_INSTANCE_ENVIRONMENT, "UP")));
    return instance;
  }
}
