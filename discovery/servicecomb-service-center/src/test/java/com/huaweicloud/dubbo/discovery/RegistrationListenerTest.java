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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.dubbo.common.URL;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.junit.Before;
import org.junit.Test;

public class RegistrationListenerTest {

  private RegistrationListener listener;

  private ServiceCenterClient client;

  private Microservice selfMicroservice;

  @Before
  public void setUp() throws Exception {
    listener = new RegistrationListener();
    client = mock(ServiceCenterClient.class);
    selfMicroservice = createMicroservice("default", "order-consumer", "1.0.0", "sid-self", "2023-01-01T00:00:00.000Z");
    setField("client", client);
    setField("microservice", selfMicroservice);
  }

  @Test
  public void filterLatestServices_shouldFilterOutDifferentApp() throws Exception {
    Microservice otherApp = createMicroservice("other-app", "price-provider", "1.0.0", "sid-other", "2023-01-01T00:00:00.000Z");
    Microservice sameApp = createMicroservice("default", "price-provider", "1.0.0", "sid-same", "2023-01-01T00:00:00.000Z");

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(otherApp, sameApp));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getServiceId()).isEqualTo("sid-same");
  }

  @Test
  public void filterLatestServices_shouldKeepLatestByTimestampWhenBothNoInstances() throws Exception {
    Microservice old = createMicroservice("default", "price-provider", "1.0.0", "sid-old", "2023-01-01T00:00:00.000Z");
    Microservice latest = createMicroservice("default", "price-provider", "2.0.0", "sid-latest", "2023-06-01T00:00:00.000Z");

    when(client.getMicroserviceInstanceList("sid-old")).thenReturn(instancesResponse(false));
    when(client.getMicroserviceInstanceList("sid-latest")).thenReturn(instancesResponse(false));

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(old, latest));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getServiceId()).isEqualTo("sid-latest");
  }

  @Test
  public void filterLatestServices_shouldKeepLatestByTimestampWhenBothHaveInstances() throws Exception {
    Microservice old = createMicroservice("default", "price-provider", "1.0.0", "sid-old", "2023-01-01T00:00:00.000Z");
    Microservice latest = createMicroservice("default", "price-provider", "2.0.0", "sid-latest", "2023-06-01T00:00:00.000Z");

    when(client.getMicroserviceInstanceList("sid-old")).thenReturn(instancesResponse(true));
    when(client.getMicroserviceInstanceList("sid-latest")).thenReturn(instancesResponse(true));

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(old, latest));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getServiceId()).isEqualTo("sid-latest");
  }

  @Test
  public void filterLatestServices_shouldKeepExistingWhenExistingHasInstancesAndNewDoesNot() throws Exception {
    Microservice existing = createMicroservice("default", "price-provider", "1.0.0", "sid-existing", "2023-01-01T00:00:00.000Z");
    Microservice newer = createMicroservice("default", "price-provider", "2.0.0", "sid-newer", "2023-06-01T00:00:00.000Z");

    when(client.getMicroserviceInstanceList("sid-existing")).thenReturn(instancesResponse(true));
    when(client.getMicroserviceInstanceList("sid-newer")).thenReturn(instancesResponse(false));

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(existing, newer));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getServiceId()).isEqualTo("sid-existing");
  }

  @Test
  public void filterLatestServices_shouldKeepNewWhenNewHasInstancesAndExistingDoesNot() throws Exception {
    Microservice existing = createMicroservice("default", "price-provider", "1.0.0", "sid-existing", "2023-06-01T00:00:00.000Z");
    Microservice newer = createMicroservice("default", "price-provider", "2.0.0", "sid-newer", "2023-01-01T00:00:00.000Z");

    when(client.getMicroserviceInstanceList("sid-existing")).thenReturn(instancesResponse(false));
    when(client.getMicroserviceInstanceList("sid-newer")).thenReturn(instancesResponse(true));

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(existing, newer));

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getServiceId()).isEqualTo("sid-newer");
  }

  @Test
  public void filterLatestServices_shouldKeepDifferentServiceNames() throws Exception {
    Microservice svcA = createMicroservice("default", "price-provider", "1.0.0", "sid-a", "2023-01-01T00:00:00.000Z");
    Microservice svcB = createMicroservice("default", "order-provider", "1.0.0", "sid-b", "2023-01-01T00:00:00.000Z");

    List<Microservice> result = invokeFilterLatestServices(Arrays.asList(svcA, svcB));

    assertThat(result).hasSize(2);
  }

  @Test
  public void hasInstances_shouldReturnTrueWhenServiceHasInstances() throws Exception {
    when(client.getMicroserviceInstanceList("sid-1")).thenReturn(instancesResponse(true));

    boolean result = invokeHasInstances("sid-1");

    assertThat(result).isTrue();
  }

  @Test
  public void hasInstances_shouldReturnFalseWhenServiceHasNoInstances() throws Exception {
    when(client.getMicroserviceInstanceList("sid-1")).thenReturn(instancesResponse(false));

    boolean result = invokeHasInstances("sid-1");

    assertThat(result).isFalse();
  }

  @Test
  public void hasInstances_shouldReturnFalseWhenInstancesIsNull() throws Exception {
    MicroserviceInstancesResponse response = new MicroserviceInstancesResponse();
    when(client.getMicroserviceInstanceList("sid-1")).thenReturn(response);

    boolean result = invokeHasInstances("sid-1");

    assertThat(result).isFalse();
  }

  @Test
  public void hasInstances_shouldReturnFalseWhenExceptionThrown() throws Exception {
    when(client.getMicroserviceInstanceList("sid-1")).thenThrow(new RuntimeException("connection refused"));

    boolean result = invokeHasInstances("sid-1");

    assertThat(result).isFalse();
  }

  @Test
  public void getServiceSchemasList_shouldReturnSchemasOnFirstSuccess() throws Exception {
    List<SchemaInfo> schemas = Collections.singletonList(new SchemaInfo());
    when(client.getServiceSchemasList("sid-1", true)).thenReturn(schemas);

    List<SchemaInfo> result = invokeGetServiceSchemasList("sid-1");

    assertThat(result).isSameAs(schemas);
    verify(client, times(1)).getServiceSchemasList("sid-1", true);
  }

  @Test
  public void getServiceSchemasList_shouldRetryAndReturnOnSecondAttempt() throws Exception {
    List<SchemaInfo> schemas = Collections.singletonList(new SchemaInfo());
    when(client.getServiceSchemasList("sid-1", true))
        .thenThrow(new RuntimeException("timeout"))
        .thenReturn(schemas);

    List<SchemaInfo> result = invokeGetServiceSchemasList("sid-1");

    assertThat(result).isSameAs(schemas);
    verify(client, times(2)).getServiceSchemasList("sid-1", true);
  }

  @Test
  public void getServiceSchemasList_shouldReturnEmptyListAfterMaxRetries() throws Exception {
    when(client.getServiceSchemasList("sid-1", true)).thenThrow(new RuntimeException("connection refused"));

    List<SchemaInfo> result = invokeGetServiceSchemasList("sid-1");

    assertThat(result).isEmpty();
    verify(client, times(3)).getServiceSchemasList("sid-1", true);
  }

  @Test
  public void getServiceSchemasList_shouldRetryThreeTimesThenReturnEmpty() throws Exception {
    when(client.getServiceSchemasList(anyString(), anyBoolean()))
        .thenThrow(new RuntimeException("fail-1"))
        .thenThrow(new RuntimeException("fail-2"))
        .thenThrow(new RuntimeException("fail-3"));

    List<SchemaInfo> result = invokeGetServiceSchemasList("sid-1");

    assertThat(result).isEmpty();
    verify(client, times(3)).getServiceSchemasList("sid-1", true);
  }

  private Microservice createMicroservice(String appId, String serviceName, String version, String serviceId,
      String timestamp) {
    Microservice microservice = new Microservice();
    microservice.setAppId(appId);
    microservice.setServiceName(serviceName);
    microservice.setVersion(version);
    microservice.setServiceId(serviceId);
    microservice.setTimestamp(timestamp);
    microservice.setSchemas(Collections.singletonList(serviceName + "-schema"));
    return microservice;
  }

  private MicroserviceInstancesResponse instancesResponse(boolean hasInstance) {
    MicroserviceInstancesResponse response = new MicroserviceInstancesResponse();
    if (hasInstance) {
      MicroserviceInstance instance = new MicroserviceInstance();
      instance.setServiceId("sid-instance");
      response.setInstances(Collections.singletonList(instance));
    } else {
      response.setInstances(Collections.emptyList());
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  private List<Microservice> invokeFilterLatestServices(List<Microservice> services) throws Exception {
    Method method = RegistrationListener.class.getDeclaredMethod("filterLatestServices", List.class);
    method.setAccessible(true);
    return (List<Microservice>) method.invoke(listener, services);
  }

  private boolean invokeHasInstances(String serviceId) throws Exception {
    Method method = RegistrationListener.class.getDeclaredMethod("hasInstances", String.class);
    method.setAccessible(true);
    return (boolean) method.invoke(listener, serviceId);
  }

  @SuppressWarnings("unchecked")
  private List<SchemaInfo> invokeGetServiceSchemasList(String serviceId) throws Exception {
    Method method = RegistrationListener.class.getDeclaredMethod("getServiceSchemasList", String.class);
    method.setAccessible(true);
    return (List<SchemaInfo>) method.invoke(listener, serviceId);
  }

  /**
   * 用例描述：订阅键未注册且实例缓存为空时，执行 register 注册订阅
   * 预制条件：interfaceMap 中存在目标 path 的 service（appId=default, serviceName=order-provider）；
   * serviceCenterDiscovery.isRegistered 返回 false，getInstanceCache 返回空列表
   * 输入参数：url path = "org.apache.dubbo.rpc.service.OrderService"
   * 预期结果：serviceCenterDiscovery.register 被调用一次，参数为新建的 (default, order-provider) 订阅键
   *
   * @throws Exception 反射注入异常
   */
  @Test
  public void registerSubscriptionIfAbsentShouldRegisterWhenInstanceCacheIsEmpty() throws Exception {
    ServiceCenterDiscovery serviceCenterDiscovery = mock(ServiceCenterDiscovery.class);
    setField("serviceCenterDiscovery", serviceCenterDiscovery);
    mapPathToMicroservice("org.apache.dubbo.rpc.service.OrderService", "default", "order-provider");
    URL url = URL.valueOf("consumer://10.0.0.1:8080/org.apache.dubbo.rpc.service.OrderService");

    listener.registerSubscriptionIfAbsent(url);

    verify(serviceCenterDiscovery, times(1))
        .register(new ServiceCenterDiscovery.SubscriptionKey("default", "order-provider"));
  }

  /**
   * 用例描述：订阅键已注册且实例缓存非空时，跳过 register 注册
   * 预制条件：interfaceMap 中存在目标 path 的 service；
   * serviceCenterDiscovery.isRegistered 返回 true，getInstanceCache 返回非空实例列表
   * 输入参数：url path = "org.apache.dubbo.rpc.service.OrderService"
   * 预期结果：serviceCenterDiscovery.register 从未被调用
   *
   * @throws Exception 反射注入异常
   */
  @Test
  public void registerSubscriptionIfAbsentShouldNotRegisterWhenCacheIsNotEmpty() throws Exception {
    ServiceCenterDiscovery serviceCenterDiscovery = mock(ServiceCenterDiscovery.class);
    setField("serviceCenterDiscovery", serviceCenterDiscovery);
    mapPathToMicroservice("org.apache.dubbo.rpc.service.OrderService", "default", "order-provider");
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setServiceId("sid-instance");
    when(serviceCenterDiscovery.isRegistered(any(ServiceCenterDiscovery.SubscriptionKey.class))).thenReturn(true);
    when(serviceCenterDiscovery.getInstanceCache(any(ServiceCenterDiscovery.SubscriptionKey.class)))
        .thenReturn(Collections.singletonList(instance));
    URL url = URL.valueOf("consumer://10.0.0.1:8080/org.apache.dubbo.rpc.service.OrderService");

    listener.registerSubscriptionIfAbsent(url);

    verify(serviceCenterDiscovery, never()).register(any(ServiceCenterDiscovery.SubscriptionKey.class));
  }

  /**
   * 用例描述：订阅键未注册（isRegistered=false）但实例缓存非空时，执行 register 注册订阅
   * 预制条件：interfaceMap 中存在目标 path 的 service；
   * serviceCenterDiscovery.isRegistered 返回 false（mock 默认值），getInstanceCache 返回非空实例列表
   * 输入参数：url path = "org.apache.dubbo.rpc.service.OrderService"
   * 预期结果：短路或运算的第二分支触发，serviceCenterDiscovery.register 被调用一次
   *
   * @throws Exception 反射注入异常
   */
  @Test
  public void registerSubscriptionIfAbsentShouldRegisterWhenNotRegisteredButCacheIsNotEmpty() throws Exception {
    ServiceCenterDiscovery serviceCenterDiscovery = mock(ServiceCenterDiscovery.class);
    setField("serviceCenterDiscovery", serviceCenterDiscovery);
    mapPathToMicroservice("org.apache.dubbo.rpc.service.OrderService", "default", "order-provider");
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setServiceId("sid-instance");
    when(serviceCenterDiscovery.getInstanceCache(any(ServiceCenterDiscovery.SubscriptionKey.class)))
        .thenReturn(Collections.singletonList(instance));
    URL url = URL.valueOf("consumer://10.0.0.1:8080/org.apache.dubbo.rpc.service.OrderService");

    listener.registerSubscriptionIfAbsent(url);

    verify(serviceCenterDiscovery, times(1))
        .register(new ServiceCenterDiscovery.SubscriptionKey("default", "order-provider"));
  }

  private void mapPathToMicroservice(String path, String appId, String serviceName) {
    mapPathToMicroservice(path, createMicroservice(appId, serviceName, "1.0.0", "sid-" + serviceName,
        "2023-01-01T00:00:00.000Z"));
  }

  @SuppressWarnings("unchecked")
  private void mapPathToMicroservice(String path, Microservice microservice) {
    try {
      Field field = RegistrationListener.class.getDeclaredField("interfaceMap");
      field.setAccessible(true);
      Object object = field.get(listener);
      if (object instanceof Map) {
        ((Map<String, Microservice>) object).put(path, microservice);
      }
    } catch (Exception e) {
      throw new IllegalStateException("failed to inject interfaceMap", e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = RegistrationListener.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(listener, value);
  }
}