/*
 *
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static com.huaweicloud.dubbo.common.CommonConfiguration.DEFAULT_PROJECT;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_INSTANCE_PULL_INTERVAL;
import static com.huaweicloud.dubbo.common.CommonConfiguration.KEY_REGISTRY_WATCH;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.ConfigUtils;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;
import com.huaweicloud.dubbo.common.AuthHeaderProviders;
import com.huaweicloud.dubbo.common.CommonConfiguration;
import com.huaweicloud.dubbo.common.EventManager;
import com.huaweicloud.dubbo.common.GovernanceData;
import com.huaweicloud.dubbo.common.GovernanceDataChangeEvent;
import com.huaweicloud.dubbo.common.ProviderInfo;
import com.huaweicloud.dubbo.common.RegistrationReadyEvent;
import com.huaweicloud.dubbo.common.SchemaInfo;

@Component
public class RegistrationListener implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware,
    EnvironmentAware {

  public static final String GENERIC_SERVICE = "org.apache.dubbo.rpc.service.GenericService";

  public static final String INTERFACE = "interface";

  static class SubscriptionKey {
    final String appId;

    final String serviceName;

    final String interfaceName;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SubscriptionKey that = (SubscriptionKey) o;
      return appId.equals(that.appId) &&
          serviceName.equals(that.serviceName) &&
          interfaceName.equals(that.interfaceName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(appId, serviceName, interfaceName);
    }

    SubscriptionKey(String appId, String serviceName, String interfaceName) {
      this.appId = appId;
      this.serviceName = serviceName;
      this.interfaceName = interfaceName;
    }
  }

  static class SubscriptionData {
    final NotifyListener notifyListener;

    final List<URL> urls;

    SubscriptionData(NotifyListener notifyListener, List<URL> urls) {
      this.notifyListener = notifyListener;
      this.urls = urls;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationListener.class);

  private ServiceCenterClient client;

  private Microservice microservice;

  private MicroserviceInstance instance;

  private ApplicationEventPublisher applicationEventPublisher;

  private final Map<String, Microservice> interfaceMap = new HashMap<>();

  private final Map<SubscriptionKey, SubscriptionData> subscriptions = new HashMap<>();

  private ServiceCenterRegistry registry;

  private ServiceCenterRegistration serviceCenterRegistration;

  private ServiceCenterDiscovery serviceCenterDiscovery;

  private ServiceCenterWatch watch;

  private final CountDownLatch firstRegistrationWaiter = new CountDownLatch(1);

  private boolean registrationInProgress = true;

  private boolean shutdown = false;

  private GovernanceData governanceData;

  private final List<NewSubscriberEvent> pendingSubscribeEvent = new ArrayList<>();

  private ServiceCenterConfigurationManager serviceCenterConfigurationManager;

  private ServiceCenterConfiguration serviceCenterConfiguration;

  private CommonConfiguration commonConfiguration;

  private Environment environment;

  public void setServiceCenterRegistry(ServiceCenterRegistry registry) {
    this.registry = registry;
  }

  public ApplicationEventPublisher applicationEventPublisher() {
    return this.applicationEventPublisher;
  }

  public void shutdown() {
    try {
      if (!shutdown) {
        serviceCenterRegistration.stop();
        serviceCenterDiscovery.stop();
        client.deleteMicroserviceInstance(microservice.getServiceId(), instance.getInstanceId());
        shutdown = true;
      }
    } catch (Exception e) {
      LOGGER.info("shutdown error.", e);
    }
  }

  @Override
  public void setEnvironment(Environment environment) {
    serviceCenterConfigurationManager = new ServiceCenterConfigurationManager(environment);
    serviceCenterConfiguration = new ServiceCenterConfiguration().setIgnoreSwaggerDifferent(
        Boolean.valueOf(environment.getProperty(CommonConfiguration.KEY_SERVICE_IGNORESWAGGERDIFFERENT, "false")));
    commonConfiguration = new CommonConfiguration(environment);
    this.environment = environment;
    watch = new ServiceCenterWatch(serviceCenterConfigurationManager.createAddressManager(),
        commonConfiguration.createSSLProperties(),
        AuthHeaderProviders.getRequestAuthHeaderProvider(commonConfiguration, environment),
        "default", new HashMap<>(), EventManager.getEventBus());
  }

  @Override
  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    if (applicationEvent instanceof ContextStartedEvent) {
      try {
        AddressManager addressManager = serviceCenterConfigurationManager.createAddressManager();
        SSLProperties sslProperties = commonConfiguration.createSSLProperties();
        client = new ServiceCenterClient(addressManager, sslProperties,
            AuthHeaderProviders.getRequestAuthHeaderProvider(commonConfiguration, environment),
            "default", null);
        microservice = serviceCenterConfigurationManager.createMicroservice();
        if (registry != null) {
          // consumer: 如果没有 provider 接口， dubbo 启动的时候， 不会初始化 Registry。 调用接口的时候，才会初始化。
          microservice
              .setSchemas(registry.getRegisters().stream().map(URL::getPath).collect(Collectors.toList()));
        }

        instance = serviceCenterConfigurationManager.createMicroserviceInstance();
        addEndpoints();
        instance.setHostName(InetAddress.getLocalHost().getHostName());

        EventManager.register(this);
        serviceCenterRegistration = new ServiceCenterRegistration(client, serviceCenterConfiguration,
            EventManager.getEventBus());
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(instance);
        serviceCenterRegistration.setHeartBeatInterval(instance.getHealthCheck().getInterval());
        addSchemaInfo(serviceCenterRegistration);
        serviceCenterRegistration.startRegistration();

        EventManager.post(new RegistrationReadyEvent());
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      waitRegistrationDone();
    } else if (applicationEvent instanceof NewSubscriberEvent) {
      NewSubscriberEvent newSubscriberEvent = (NewSubscriberEvent) applicationEvent;
      // 第一次订阅， 按照 dubbo 的要求， 需要查询实例列表
      if (registrationInProgress) {
        pendingSubscribeEvent.add(newSubscriberEvent);
        return;
      }

      processNewSubscriberEvent(newSubscriberEvent);
    }
  }

  private void addEndpoints() {
    Set<String> endpoints = new HashSet<>();
    if (registry != null) {
      for (URL url : registry.getRegisters()) {
        URL newUrl = new URL(url.getProtocol(), url.getHost(), url.getPort());
        endpoints.add(newUrl.toString());
      }
    }
    instance.setEndpoints(new ArrayList<>(endpoints));
  }


  private void addSchemaInfo(ServiceCenterRegistration registration) {
    if (registry != null) {
      // consumer: 如果没有 provider 接口， dubbo 启动的时候， 不会初始化 Registry。 调用接口的时候，才会初始化。
      microservice.setSchemas(registry.getRegisters().stream().map(URL::getPath).collect(Collectors.toList()));
      registration.setSchemaInfos(
          registry.getRegisters().stream().map(this::createSchemaInfo).collect(Collectors.toList()));
    }
  }

  private org.apache.servicecomb.service.center.client.model.SchemaInfo createSchemaInfo(URL url) {
    URL newUrl = url.setHost(microservice.getServiceName());
    org.apache.servicecomb.service.center.client.model.SchemaInfo info
        = new org.apache.servicecomb.service.center.client.model.SchemaInfo();
    info.setSchemaId(newUrl.getPath());
    info.setSchema(newUrl.toString());
    info.setSummary(calcSchemaSummary(info.getSchema()));
    return info;
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }

  private void processNewSubscriberEvent(NewSubscriberEvent newSubscriberEvent) {
    Microservice microservice = interfaceMap.get(newSubscriberEvent.getUrl().getPath());
    if (microservice == null) {
      // provider 后于 consumer 启动的场景， 再查询一次。
      updateInterfaceMap();
      if (newSubscriberEvent.getUrl().getPath().equals(GENERIC_SERVICE)) {
        microservice = interfaceMap.get(newSubscriberEvent.getUrl().getParameter(INTERFACE));
      } else {
        microservice = interfaceMap.get(newSubscriberEvent.getUrl().getPath());
      }
    }
    if (microservice == null) {
      LOGGER.error("the subscribe url [{}] is not registered.", newSubscriberEvent.getUrl().getPath());
      pendingSubscribeEvent.add(newSubscriberEvent);
      return;
    }
    MicroserviceInstancesResponse instancesResponse = client
        .getMicroserviceInstanceList(microservice.getServiceId());
    subscriptions.put(new SubscriptionKey(microservice.getAppId(), microservice.getServiceName(),
            newSubscriberEvent.getUrl().getPath()),
        new SubscriptionData(newSubscriberEvent.getNotifyListener(), new ArrayList<>()));
    // 第一次订阅， 按照 dubbo 的要求， 需要查询实例列表
    notify(microservice.getAppId(), microservice.getServiceName(), instancesResponse.getInstances());
    serviceCenterDiscovery
        .register(new ServiceCenterDiscovery.SubscriptionKey(microservice.getAppId(), microservice.getServiceName()));
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  private void waitRegistrationDone() {
    try {
      firstRegistrationWaiter.await(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOGGER.warn("registration is not finished in 30 seconds.");
    }
  }

  private void updateInterfaceMap() {
    try {
      Map<String, Microservice> result = new HashMap<>();
      MicroservicesResponse microservicesResponse = client.getMicroserviceList();
      microservicesResponse.getServices().forEach(service -> {
        // 先不考虑运行 crossAPP 的场景， 只允许同应用发现
        if (service.getAppId().equals(this.microservice.getAppId())) {
          service.getSchemas().forEach(schema -> {
            if (result.containsKey(schema)) {
              LOGGER.warn("found duplicate schema {} in microservice {} and {}", schema, service.getServiceName()
                      + ":" + service.getVersion(),
                  result.get(schema).getServiceName() + ":" + result.get(schema).getVersion());
            }
            result.put(schema, service);
          });
        }
      });
      interfaceMap.clear();
      interfaceMap.putAll(result);
    } catch (Exception e) {
      LOGGER.error("update interface - service name map failed.", e);
    }
  }

  // --- 注册事件处理 ---- //
  @Subscribe
  public void onHeartBeatEvent(HeartBeatEvent event) {
    if (event.isSuccess()) {
      registrationInProgress = false;
      processPendingEvent();
    }
  }

  private void processPendingEvent() {
    List<NewSubscriberEvent> events = new ArrayList<>(pendingSubscribeEvent.size());
    events.addAll(pendingSubscribeEvent);
    pendingSubscribeEvent.clear();
    events.forEach(this::processNewSubscriberEvent);
  }

  @Subscribe
  public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
    registrationInProgress = true;
    if (event.isSuccess()) {
      if (serviceCenterDiscovery == null) {
        serviceCenterDiscovery = new ServiceCenterDiscovery(client, EventManager.getEventBus());
        serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
        serviceCenterDiscovery
            .setPollInterval(Integer.parseInt(environment.getProperty(KEY_INSTANCE_PULL_INTERVAL, "15")));
        serviceCenterDiscovery.startDiscovery();
      } else {
        serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
      }
    }
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    registrationInProgress = true;
    if (event.isSuccess()) {
      if (Boolean.parseBoolean(ConfigUtils.getProperty(KEY_REGISTRY_WATCH, ""))) {
        watch.startWatch(ConfigUtils.getProperty(DEFAULT_PROJECT, "default"), microservice.getServiceId());
      }
      updateInterfaceMap();
      firstRegistrationWaiter.countDown();
    }
  }
  // --- END ---- //

  // --- 实例发现事件处理 ---- //
  @Subscribe
  public void onInstanceChangedEvent(InstanceChangedEvent event) {
    notify(event.getAppName(), event.getServiceName(), event.getInstances());
  }

  // --- END ---- //

  // --- 治理配置变更事件 ---- //
  @Subscribe
  public void onGovernanceDataChangeEvent(GovernanceDataChangeEvent event) {
    this.governanceData = event.getGovernanceData();
    this.subscriptions.forEach((k, v) -> v.notifyListener.notify(wrapGovernanceData(k, v.urls)));
  }

  private void notify(String appId, String serviceName, List<MicroserviceInstance> instances) {
    if (instances == null) {
      return;
    }
    Map<String, List<URL>> notifyUrls = instancesToURLs(instances);

    notifyUrls.forEach((k, v) -> {
      SubscriptionKey subscriptionKey = new SubscriptionKey(appId, serviceName, k);
      SubscriptionData subscriptionData = this.subscriptions.get(subscriptionKey);
      // consumer 没有订阅所有接口的场景， subscriptionData 可能为 null
      if (subscriptionData != null) {
        subscriptionData.urls.clear();
        subscriptionData.urls.addAll(v);
        subscriptionData.notifyListener.notify(wrapGovernanceData(subscriptionKey, v));
      }
    });
  }

  private Map<String, List<URL>> instancesToURLs(List<MicroserviceInstance> instances) {
    Map<String, List<URL>> notifyUrls = new HashMap<>();

    instances.forEach(instance -> {
      List<org.apache.servicecomb.service.center.client.model.SchemaInfo> schemaInfos = client
          .getServiceSchemasList(instance.getServiceId(), true);
      instance.getEndpoints().forEach(e -> {
        URL url = URL.valueOf(e);
        if (schemaInfos.isEmpty()) {
          // old version new schema info
          notifyUrls.putIfAbsent(url.getPath(), new ArrayList<>());
          notifyUrls.get(url.getPath()).add(url);
          return;
        }
        // parameters are in schema info
        schemaInfos.forEach(schema -> {
          URL newUrl = URL.valueOf(schema.getSchema());
          if (!newUrl.getProtocol().equals(url.getProtocol())) {
            return;
          }
          notifyUrls.putIfAbsent(newUrl.getPath(), new ArrayList<>());
          notifyUrls.get(newUrl.getPath()).add(newUrl.setHost(url.getHost()).setPort(url.getPort()));

          notifyUrls.putIfAbsent(GENERIC_SERVICE, new ArrayList<>());
          notifyUrls.get(GENERIC_SERVICE).add(newUrl.setHost(url.getHost()).setPort(url.getPort()));
        });
      });
    });

    return notifyUrls;
  }

  private List<URL> wrapGovernanceData(SubscriptionKey subscriptionKey, List<URL> urls) {
    if (governanceData == null || governanceData.getProviderInfos() == null) {
      return urls;
    }

    for (ProviderInfo providerInfo : governanceData.getProviderInfos()) {
      for (SchemaInfo schemaInfo : providerInfo.getSchemaInfos()) {
        SubscriptionKey tempSubscriptionKey = new SubscriptionKey(microservice.getAppId(),
            providerInfo.getServiceName(), schemaInfo.getSchemaId());
        if (tempSubscriptionKey.equals(subscriptionKey)) {
          SubscriptionData subscriptionData = this.subscriptions.get(subscriptionKey);
          if (subscriptionData != null) {
            List<URL> result = new ArrayList<>(urls.size());
            for (URL url : urls) {
              Map<String, String> parameters = new HashMap<>();
              parameters.putAll(url.getParameters());
              parameters.putAll(schemaInfo.getParameters());
              URL newUrl = new URL(url.getProtocol(), url.getUsername(), url.getPassword(), url.getHost(), url.getPort()
                  , url.getPath(), parameters);
              result.add(newUrl);
            }
            return result;
          }
        }
      }
    }
    return urls;
  }

  public void registerSubscriptionIfAbsent(URL url) {
    Microservice service = interfaceMap.get(url.getPath());
    ServiceCenterDiscovery.SubscriptionKey subscriptionKey = new ServiceCenterDiscovery.SubscriptionKey(
        service.getAppId(), service.getServiceName());
    if (!serviceCenterDiscovery.isRegistered(subscriptionKey)) {
      serviceCenterDiscovery.register(subscriptionKey);
    }
  }
}
