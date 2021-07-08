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

import com.google.common.eventbus.EventBus;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.KieConfigOperation;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;

import java.util.Map;

public class KieConfigManagerExt extends KieConfigManager {

    private Map<String,Object> lastConfiguration;

    private ConfigurationsRequest configurationsRequest;

    public ConfigurationsRequest getConfigurationsRequest() {
        return configurationsRequest;
    }

    public KieConfigManager setConfigurationsRequest(ConfigurationsRequest configurationsRequest) {
        this.configurationsRequest = configurationsRequest;
        return this;
    }

    public void setLastConfiguration(Map<String, Object> lastConfiguration) {
        this.lastConfiguration = lastConfiguration;
    }

    public Map<String, Object> getLastConfiguration() {
        return lastConfiguration;
    }

    public KieConfigManagerExt(KieConfigOperation configKieClient, KieConfiguration kieConfiguration, ConfigConverter configConverter, EventBus eventBus, Map<String, Object> lastConfiguration) {
        super(configKieClient,eventBus,kieConfiguration,configConverter);
        this.lastConfiguration = lastConfiguration;
    }


}
