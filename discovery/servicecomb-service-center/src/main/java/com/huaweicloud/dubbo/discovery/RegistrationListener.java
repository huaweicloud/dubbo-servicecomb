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

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Component
public class RegistrationListener implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {
  class SubscriptionKey {
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

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationListener.class);

  private ServiceCenterClient client;

  private Microservice microservice;

  private MicroserviceInstance instance;

  private ApplicationEventPublisher applicationEventPublisher;

  private Map<String, Microservice> interfaceMap = new HashMap<>();

  private Map<SubscriptionKey, NotifyListener> subscriptions = new HashMap<>();

  private ServiceCenterRegistry registry;

  private ServiceCenterRegistration serviceCenterRegistration;

  private ServiceCenterDiscovery serviceCenterDiscovery;

  private EventBus eventBus;

  private CountDownLatch firstRegistrationWaiter = new CountDownLatch(1);

  private boolean registrationInProgress = true;

  private boolean shutdown = false;

  public RegistrationListener() {
  }

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
  @SuppressWarnings("unchecked")
  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    if (applicationEvent instanceof ContextRefreshedEvent) {
      try {
        AddressManager addressManager = new AddressManager("default", Arrays.asList("http://127.0.0.1:30100"));
        SSLProperties sslProperties = new SSLProperties();
        sslProperties.setEnabled(false);
        client = new ServiceCenterClient(addressManager, sslProperties, "default", null);
        microservice = Configuration.createMicroservice();
        if (registry != null) {
          // consumer: 如果没有 provider 接口， dubbo 启动的时候， 不会初始化 Registry。 调用接口的时候，才会初始化。
          microservice
              .setSchemas(registry.getRegisters().stream().map(url -> url.getPath()).collect(Collectors.toList()));
        }

        // 不需要注册 schema, 放到实例里面
//        List<SchemaInfo> schemaInfos = new ArrayList<>();
//
//        if (registry != null) {
//          registry.getRegisters().forEach(url -> {
//            SchemaInfo schemaInfo = new SchemaInfo();
//            schemaInfo.setSchemaId(url.getPath());
//            schemaInfo.setSchema(url.toString());
//            schemaInfo
//                .setSummary(Hashing.sha256().newHasher().putString(url.toString(), Charsets.UTF_8).hash().toString());
//            schemaInfos.add(schemaInfo);
//          });
//        }

        instance = new MicroserviceInstance();
        List<String> endpoints = new ArrayList<>();
        if (registry != null) {
          endpoints.addAll(registry.getRegisters().stream().map(url -> url.toString()).collect(Collectors.toList()));
        }
        instance.setEndpoints(endpoints);
        instance.setHostName(InetAddress.getLocalHost().getHostName());

        eventBus = new EventBus();
        eventBus.register(this);
        serviceCenterRegistration = new ServiceCenterRegistration(client, eventBus);
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(instance);
//        serviceCenterRegistration.setSchemaInfos(schemaInfos);
        serviceCenterRegistration.startRegistration();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      waitRegistrationDone();
    } else if (applicationEvent instanceof NewSubscriberEvent) {
      NewSubscriberEvent newSubscriberEvent = (NewSubscriberEvent) applicationEvent;
      // 第一次订阅， 按照 dubbo 的要求， 需要查询实例列表
      if (newSubscriberEvent.getUrl().getProtocol().equals("consumer")) {
        if (registrationInProgress) {
          LOGGER.warn("registration is in progress, can not subscribe new consumers. ");
          return;
        }

        Microservice microservice = interfaceMap.get(newSubscriberEvent.getUrl().getPath());
        if (microservice == null) {
          // provider 后于 consumer 启动的场景， 再查询一次。
          updateInterfaceMap();
          microservice = interfaceMap.get(newSubscriberEvent.getUrl().getPath());
        }
        if (microservice == null) {
          LOGGER.error("the subscribe url [{}] is not registered.", newSubscriberEvent.getUrl().getPath());
          return;
        }
        MicroserviceInstancesResponse instancesResponse = client
            .getMicroserviceInstanceList(microservice.getServiceId());
        subscriptions.put(new SubscriptionKey(microservice.getAppId(), microservice.getServiceName(),
                newSubscriberEvent.getUrl().getPath()),
            newSubscriberEvent.getNotifyListener());
        notify(microservice.getAppId(), microservice.getServiceName(), instancesResponse.getInstances());
        serviceCenterDiscovery.register(microservice);
      }
    }
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
        service.getSchemas().forEach(schema -> result.put(schema, service));
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
    }
  }

  @Subscribe
  public void onMicroserviceRegistrationEvent(MicroserviceRegistrationEvent event) {
    registrationInProgress = true;
    if (event.isSuccess()) {
      if (serviceCenterDiscovery == null) {
        serviceCenterDiscovery = new ServiceCenterDiscovery(client, eventBus);
        serviceCenterDiscovery.startDiscovery();
      }
      serviceCenterDiscovery.updateMySelf(microservice);
    }
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    registrationInProgress = true;
    if (event.isSuccess()) {
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

  private void notify(String appId, String serviceName, List<MicroserviceInstance> instances) {
    Map<String, List<URL>> notifyUrls = new HashMap<>();

    instances.forEach(instance -> {
      instance.getEndpoints().forEach(e -> {
        URL url = URL.valueOf(e);
        notifyUrls.putIfAbsent(url.getPath(), new ArrayList<>());
        notifyUrls.get(url.getPath()).add(url);
      });
    });

    notifyUrls.forEach((k, v) -> {
      SubscriptionKey subscriptionKey = new SubscriptionKey(appId, serviceName, k);
      NotifyListener listener = this.subscriptions.get(subscriptionKey);
      if (listener != null) {
        // consumer 没有订阅所有接口的场景
        listener.notify(v);
      }
    });
  }
  // --- END ---- //
}
