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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.apache.servicecomb.service.center.client.model.ModifySchemasRequest;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

@Component
public class RegistrationListener implements ApplicationListener<ApplicationEvent>, ApplicationEventPublisherAware {
  private boolean registered = false;

  private ServiceCenterClient client;

  private Microservice microservice;

  private MicroserviceInstance instance;

  private ApplicationEventPublisher applicationEventPublisher;

  Map<String, String> interfaceMap;

  public RegistrationListener() {
  }

  public ApplicationEventPublisher applicationEventPublisher() {
    return this.applicationEventPublisher;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    if (applicationEvent instanceof ContextRefreshedEvent) {
      try {
        ServiceCenterRegistry registry = ServiceCenterRegistry.getInstance();

        client = new ServiceCenterClient();
        microservice = Configuration.createMicroservice();
        if (registry != null) {
          // consumer: 如果没有 provider 接口， dubbo 启动的时候， 不会初始化 Registry。 调用接口的时候，才会初始化。
          microservice
              .setSchemas(registry.getRegisters().stream().map(url -> url.getPath()).collect(Collectors.toList()));
        }
        String serviceResponse = client.registerMicroservice(microservice);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> response =
            (Map<String, Object>) objectMapper
                .readValue(serviceResponse.getBytes(Charset.forName("UTF-8")), Object.class);
        microservice.setServiceId(response.get("serviceId").toString());

        List<SchemaInfo> schemaInfos = new ArrayList<>();

        if (registry != null) {
          registry.getRegisters().forEach(url -> {
            SchemaInfo schemaInfo = new SchemaInfo();
            schemaInfo.setSchemaId(url.getPath());
            schemaInfo.setSchema(url.toString());
            schemaInfo
                .setSummary(Hashing.sha256().newHasher().putString(url.toString(), Charsets.UTF_8).hash().toString());
            schemaInfos.add(schemaInfo);
          });
        }

        if (!schemaInfos.isEmpty()) {
          ModifySchemasRequest modifySchemasRequest = new ModifySchemasRequest();
          modifySchemasRequest.setSchemas(schemaInfos);
          client.batchUpdateServiceSchemaContext(response.get("serviceId").toString(), modifySchemasRequest);
        }

        instance = new MicroserviceInstance();
        instance.setServiceId(microservice.getServiceId());
        List<String> endpoints = new ArrayList<>();
        if (registry != null) {
          endpoints.addAll(registry.getRegisters().stream().map(url -> url.toString()).collect(Collectors.toList()));
        }
        instance.setEndpoints(endpoints);
        instance.setHostName(InetAddress.getLocalHost().getHostName());
        String instanceId = client.registerMicroserviceInstance(instance, microservice.getServiceId());
        instance.setInstanceId(instanceId);

        MicroservicesResponse microservicesResponse = client.getMicroserviceList();
        interfaceMap = new HashMap<>();
        microservicesResponse.getServices().forEach(service -> {
          service.getSchemas().forEach(schema -> interfaceMap.put(schema, service.getServiceId()));
        });

        registered = true;
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else if (applicationEvent instanceof NewSubscriberEvent) {
      if (registered) {
        ServiceCenterRegistry registry = ServiceCenterRegistry.getInstance();
        Map<URL, NotifyListener> subscribers = registry.getSubscribers();
        subscribers.forEach((k, v) -> {
          if (k.getProtocol().equals("consumer")) {
            String serviceID = interfaceMap.get(k.getPath());
            MicroserviceInstancesResponse instancesResponse = client.getMicroserviceInstanceList(serviceID);
            List<URL> notifyUrls = new ArrayList<>();
            instancesResponse.getInstances().forEach(instance -> {
              instance.getEndpoints().forEach(e -> notifyUrls.add(URL.valueOf(e)));
            });
            v.notify(notifyUrls);
          }
        });
      }
    }
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
}
