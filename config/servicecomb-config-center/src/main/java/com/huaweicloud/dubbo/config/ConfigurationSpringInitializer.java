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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.KieConfigOperation;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.dubbo.common.CommonConfiguration;
import com.huaweicloud.dubbo.common.EventManager;
import com.huaweicloud.dubbo.common.GovernanceData;
import com.huaweicloud.dubbo.common.GovernanceDataChangeEvent;
import com.huaweicloud.dubbo.common.RegistrationReadyEvent;

import org.springframework.util.StringUtils;

@Component
public class ConfigurationSpringInitializer extends PropertyPlaceholderConfigurer implements EnvironmentAware{
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSpringInitializer.class);

  public static final String CONFIG_NAME = "config-center-property-source";

  ConfigCenterManager configCenterManager;

  KieConfigManager kieConfigManager;

  QueryConfigurationsRequest queryConfigurationsRequest;

  ConfigurationsRequest configurationsRequest;

  HttpTransport httpTransport;

  final Map<String, Object> sources = new HashMap<>();

  private Map<String, Object> configurations = new HashMap<>();

  private String governanceData = null;

  private boolean isKie = false;

  private int pollingWaitInSeconds = 0;

  private ConfigCenterConfiguration configCenterConfiguration;

  private KieConfigConfiguration kieConfigConfiguration;

  private CommonConfiguration commonConfiguration;

  private KieConfiguration kieConfiguration;

  private ConfigConverter configConverter;

  public ConfigurationSpringInitializer() {
    configConverter = initConfigConverter();
    setOrder(Ordered.LOWEST_PRECEDENCE / 2);
    setIgnoreUnresolvablePlaceholders(true);
  }

  @Override
  public void setEnvironment(Environment environment) {
    configCenterConfiguration = new ConfigCenterConfiguration(environment);
    kieConfigConfiguration = new KieConfigConfiguration(environment);
    commonConfiguration = new CommonConfiguration(environment);

    if (!(environment instanceof ConfigurableEnvironment)) {
      return;
    }

    ConfigurableEnvironment ce = (ConfigurableEnvironment) environment;

    addDubboProperties(ce);

    addConfigCenterProperties(ce);
  }

  private ConfigConverter initConfigConverter() {
    String fileSources = ConfigUtils.getProperty(CommonConfiguration.KEY_CONFIG_FILESOURCE);
    if (StringUtils.isEmpty(fileSources)) {
      configConverter = new ConfigConverter(null);
    } else {
      configConverter = new ConfigConverter(Arrays.asList(fileSources.split("，")));
    }
    return configConverter;
  }

  private void addConfigCenterProperties(ConfigurableEnvironment ce) {
    if (configCenterPropertySourceExists(ce)) {
      return;
    }
    isKie = ConfigUtils.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESSTYPE, "").equals("kie");
    RequestConfig.Builder config = HttpTransportFactory.defaultRequestConfig();

    this.setTimeOut(config);

    httpTransport = HttpTransportFactory
        .createHttpTransport(commonConfiguration.createSSLProperties(),
            commonConfiguration.createRequestAuthHeaderProvider(), config.build());
    //判断是否使用KIE作为配置中心
    if (isKie) {
      kieConfiguration = kieConfigConfiguration.createKieConfiguration();
      int pollingWaitSec = kieConfiguration.getPollingWaitInSeconds();
      if (kieConfiguration.isEnableLongPolling() && pollingWaitSec >= 0){
        config.setConnectionRequestTimeout(pollingWaitSec * 2 * 1000);
        config.setSocketTimeout(pollingWaitSec * 2 * 1000);
      }
      configKieClient(ce);
    } else {
      configCenterClient(ce);
    }
  }

  private void addDubboProperties(ConfigurableEnvironment ce) {
    try {
      EncodedResource resource = new EncodedResource(new ClassPathResource("dubbo.properties"));
      ce.getPropertySources().addLast(new ResourcePropertySource(resource));
    } catch (IOException e) {
      LOGGER.error("add dubbo.properties failed.", e);
    }
  }

  private void configCenterClient(ConfigurableEnvironment ce) {
    queryConfigurationsRequest = configCenterConfiguration.createQueryConfigurationsRequest();
    AddressManager addressManager = configCenterConfiguration.createAddressManager();
    if (addressManager == null) {
      LOGGER.warn("Config center address is not configured and will not enable dynamic config.");
      return;
    }
    ConfigCenterClient configCenterClient = new ConfigCenterClient(addressManager, httpTransport);
    try {
      QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest);
      if (response.isChanged()) {
        configConverter.updateData(response.getConfigurations());
      }
      queryConfigurationsRequest.setRevision(response.getRevision());
      sources.putAll(configConverter.getCurrentData());
      ce.getPropertySources().addFirst(new MapPropertySource(CONFIG_NAME, sources));
    } catch (Exception e) {
      LOGGER.warn("set up {} failed at startup.", CONFIG_NAME, e);
    }
    configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(), configConverter);
    EventManager.register(this);
    configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
    configCenterManager.startConfigCenterManager();
  }

  //use KIE as config center
  private void configKieClient(ConfigurableEnvironment ce) {
    configurationsRequest = kieConfigConfiguration.createConfigurationsRequest();
    KieAddressManager kieAddressManager = kieConfigConfiguration.createKieAddressManager();
    if (kieAddressManager == null) {
      LOGGER.warn("Kie address is not configured and will not enable dynamic config.");
      return;
    }

    KieConfigOperation kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);

//    try {
//      ConfigurationsResponse response = kieClient.queryConfigurations(configurationsRequest);
//      if (response.isChanged()) {
//        configConverter.updateData(response.getConfigurations());
//      }
//      configurationsRequest.setRevision(response.getRevision());
//      sources.putAll(configConverter.getCurrentData());
//      ce.getPropertySources().addFirst(new MapPropertySource(CONFIG_NAME, sources));
//    } catch (Exception e) {
//      LOGGER.warn("set up {} failed at startup.", CONFIG_NAME, e);
//    }

    kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(),
        kieConfiguration, configConverter);
    kieConfigManager.firstPull();
    EventManager.register(this);
    kieConfigManager.startConfigKieManager();
  }

  private void setTimeOut(RequestConfig.Builder config) {
    if (!isKie) {
      return;
    }
    String test = ConfigUtils.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING,
        "true");
    if (Boolean.parseBoolean(test)) {
      pollingWaitInSeconds = Integer.valueOf(ConfigUtils.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC,
          "30"));
      config.setSocketTimeout(pollingWaitInSeconds * 1000 + 5000);
    }
  }

  private boolean configCenterPropertySourceExists(ConfigurableEnvironment ce) {
    return ce.getPropertySources().contains(CONFIG_NAME);
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    LOGGER.info("receive new configurations, added=[{}], updated=[{}], deleted=[{}]",
        event.getAdded().keySet(),
        event.getUpdated().keySet(),
        event.getDeleted().keySet());
    updatePropertySourceData(event);
    notifyGovernanceDataChange(event);
  }

  private void updatePropertySourceData(ConfigurationChangedEvent event){
    try {
      if (!event.getDeleted().isEmpty()){
        System.out.println("updatePropertySourceData====event.getDeleted()" + event.getDeleted().keySet());
        System.out.println("删除前："+sources);
        event.getDeleted().forEach((k,v) -> sources.remove(k));
        System.out.println("删除后："+sources);
      }
      if (!event.getAdded().isEmpty()){
        System.out.println("updatePropertySourceData====event.getAdded()" + event.getAdded());
        System.out.println("添加前："+sources);
        sources.putAll(event.getAdded());
        System.out.println("添加后："+sources);
      }
      if (!event.getUpdated().isEmpty()){
        System.out.println("updatePropertySourceData====event.getUpdated()" + event.getUpdated());
        System.out.println("更新前："+sources);
        sources.putAll(event.getUpdated());
        System.out.println("更新前："+sources);
      }
    } catch (Exception e) {
      LOGGER.error("",e);
    }
  }


  @Override
  protected Properties mergeProperties() throws IOException {
    Properties properties = super.mergeProperties();
    properties.putAll(this.sources);
    return properties;
  }

  @Override
  protected String resolvePlaceholder(String placeholder, Properties props) {
    String propertyValue = super.resolvePlaceholder(placeholder, props);
    if (propertyValue == null) {
      return this.sources.get(placeholder) == null ? null : this.sources.get(placeholder).toString();
    }
    return propertyValue;
  }

  @Subscribe
  public void onRegistrationReadyEvent(RegistrationReadyEvent event) {
    // 注册完成发送一次配置变更， 保证订阅者能够读取到配置
    try {
      if (this.governanceData == null) {
        EventManager.post(new GovernanceDataChangeEvent(null));
      } else {
        EventManager
            .post(new GovernanceDataChangeEvent(HttpUtils.deserialize(this.governanceData, GovernanceData.class)));
      }
      Class<?> configurationChangedEventClass = Class
          .forName("org.apache.servicecomb.config.common.ConfigurationChangedEvent");
      Class<?>[] paramTypes = {Map.class, Map.class, Map.class};
      Object[] params = {configurations, Collections.emptyMap(), Collections.emptyMap()};
      Constructor<?> constructor = configurationChangedEventClass.getDeclaredConstructor(paramTypes);
      constructor.setAccessible(true);
      Object obj = constructor.newInstance(params);
      EventManager.post(obj);
    } catch (IOException e) {
      LOGGER.error("wrong governance data [{}] received.", this.governanceData);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void notifyGovernanceDataChange(ConfigurationChangedEvent event) {
    Map<String, Object> configurations = new HashMap<String, Object>(
        event.getAdded().size() + event.getUpdated().size());
    event.getDeleted().keySet().forEach(k -> configurations.remove(event.getDeleted().keySet()));
    configurations.putAll(event.getAdded());
    configurations.putAll(event.getUpdated());
    String governanceData = (String) configurations.get(GovernanceDataChangeEvent.GOVERNANCE_KEY);
    if (isGovernanceDataChanged(governanceData)) {
      try {
        if (governanceData == null) {
          EventManager.post(new GovernanceDataChangeEvent(null));
        } else {
          EventManager.post(new GovernanceDataChangeEvent(HttpUtils.deserialize(governanceData, GovernanceData.class)));
        }
        this.governanceData = governanceData;
      } catch (IOException e) {
        LOGGER.error("wrong governance data [{}] received.", governanceData);
      }
    }
  }

  private boolean isGovernanceDataChanged(String governanceData) {
    return (this.governanceData == null && governanceData != null)
        || (this.governanceData != null && !this.governanceData.equals(governanceData));
  }
}