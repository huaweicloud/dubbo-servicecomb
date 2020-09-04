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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;

public class ServiceCenterRegistry extends FailbackRegistry {
  private static ServiceCenterRegistry instance;


  private List<URL> registers;

  private Map<URL, NotifyListener> subscribers;

  private RegistrationListener registrationListener;

  // 方便被 spring 相关的类使用
  static ServiceCenterRegistry getInstance() {
    return instance;
  }

  public ServiceCenterRegistry(URL url, RegistrationListener registrationListener) {
    super(url);

    instance = this;
    this.registrationListener = registrationListener;
    this.registers = new ArrayList<>();
    this.subscribers = new HashMap<>();
  }

  @Override
  protected void doRegister(URL url) {

    this.registers.add(url);
  }

  @Override
  protected void doUnregister(URL url) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  protected void doSubscribe(URL url, NotifyListener notifyListener) {
    this.subscribers.put(url, notifyListener);
    this.registrationListener.applicationEventPublisher().publishEvent(new NewSubscriberEvent(""));
  }

  @Override
  protected void doUnsubscribe(URL url, NotifyListener notifyListener) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public boolean isAvailable() {
    throw new IllegalStateException("not implemented");
  }


  public List<URL> getRegisters() {
    return registers;
  }

  public Map<URL, NotifyListener> getSubscribers() {
    return subscribers;
  }
}
