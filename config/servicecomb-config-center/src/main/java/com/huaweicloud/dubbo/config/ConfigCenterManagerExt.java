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
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.common.ConfigConverter;

import java.util.Map;

public class ConfigCenterManagerExt extends ConfigCenterManager {

    private Map<String,Object> lastConfiguration;

    public void setLastConfiguration(Map<String, Object> lastConfiguration) {
        this.lastConfiguration = lastConfiguration;
    }

    public ConfigCenterManagerExt(ConfigCenterClient configCenterClient, EventBus eventBus, ConfigConverter configConverter, Map<String,Object> lastConfiguration) {
        super(configCenterClient,eventBus,configConverter);
        this.lastConfiguration = lastConfiguration;
    }
}
