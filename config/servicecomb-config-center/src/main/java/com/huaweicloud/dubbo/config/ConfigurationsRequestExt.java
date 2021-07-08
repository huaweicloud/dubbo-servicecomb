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

import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;

public class ConfigurationsRequestExt extends ConfigurationsRequest {

    private String application;

    private String serviceName;

    private String version;

    private String environment;

    public ConfigurationsRequestExt setApplication(String application) {
        this.application = application;
        return this;
    }

    public ConfigurationsRequestExt setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ConfigurationsRequestExt setVersion(String version) {
        this.version = version;
        return this;
    }

    public ConfigurationsRequestExt setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getApplication() {
        return application;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    public String getEnvironment() {
        return environment;
    }
}
