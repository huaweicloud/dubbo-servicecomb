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

import java.util.ArrayList;
import java.util.List;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.support.FailbackRegistry;

public class ServiceCenterRegistry extends FailbackRegistry {
  private static final String PROTOCOL_CONSUMER = "consumer";

  private List<URL> registers;

  private RegistrationListener registrationListener;

  public ServiceCenterRegistry(URL url, RegistrationListener registrationListener) {
    super(url);
    this.registrationListener = registrationListener;
    this.registrationListener.setServiceCenterRegistry(this);
    this.registers = new ArrayList<>();
  }

  @Override
  public void doRegister(URL url) {
    if (!url.getProtocol().equals(PROTOCOL_CONSUMER)) {
      this.registers.add(url);
    }
  }

  @Override
  public void doUnregister(URL url) {
    this.registrationListener.shutdown();
  }

  @Override
  public void doSubscribe(URL url, NotifyListener notifyListener) {
    if (url.getProtocol().equals(PROTOCOL_CONSUMER)) {
      this.registrationListener.applicationEventPublisher().publishEvent(new NewSubscriberEvent(url, notifyListener));
    }
  }

  @Override
  public void doUnsubscribe(URL url, NotifyListener notifyListener) {
    this.registrationListener.shutdown();
  }

  @Override
  public boolean isAvailable() {
    throw new IllegalStateException("not implemented");
  }


  public List<URL> getRegisters() {
    return registers;
  }
}
