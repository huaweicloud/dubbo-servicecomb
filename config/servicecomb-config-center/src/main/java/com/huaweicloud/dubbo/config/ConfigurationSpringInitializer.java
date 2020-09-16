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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.ConfigurationChangedEvent;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.dubbo.common.CommonConfiguration;
import com.huaweicloud.dubbo.common.EventManager;

public class ConfigurationSpringInitializer extends PropertyPlaceholderConfigurer implements EnvironmentAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

  public static final String CONFIG_NAME = "config-center-property-source";

  ConfigCenterManager configCenterManager;

  ConfigCenterClient configCenterClient;

  QueryConfigurationsRequest queryConfigurationsRequest;

  final Map<String, Object> sources = new HashMap<>();

  public ConfigurationSpringInitializer() {
    setOrder(Ordered.LOWEST_PRECEDENCE / 2);
    setIgnoreUnresolvablePlaceholders(true);
  }

  @Override
  public void setEnvironment(Environment environment) {
    if (environment instanceof ConfigurableEnvironment) {
      ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;
      if (!configCenterPropertySourceExists(ce)) {
        AddressManager addressManager = ConfigCenterConfiguration.createAddressManager();
        HttpTransport httpTransport = HttpTransportFactory
            .createHttpTransport(CommonConfiguration.createSSLProperties(), CommonConfiguration.createAKSKProperties());
        configCenterClient = new ConfigCenterClient(addressManager, httpTransport);
        queryConfigurationsRequest = ConfigCenterConfiguration.createQueryConfigurationsRequest();

        try {
          QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest);
          queryConfigurationsRequest.setRevision(response.getRevision());
          sources.putAll(response.getConfigurations());
          ce.getPropertySources().addFirst(
              new MapPropertySource(CONFIG_NAME, sources));
        } catch (Exception e) {
          LOGGER.warn("set up {} failed at startup.", CONFIG_NAME, e);
        }

        configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus());
        EventManager.register(this);
        configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
        configCenterManager.startConfigCenterManager();
      }
    }
  }

  private boolean configCenterPropertySourceExists(ConfigurableEnvironment ce) {
    return ce.getPropertySources().contains(CONFIG_NAME);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    LOGGER.info("receive new configurations [{}]", event.getConfigurations().keySet());
    sources.clear();
    sources.putAll(event.getConfigurations());
  }
}