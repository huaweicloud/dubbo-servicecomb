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

package com.huaweicloud.it.price;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;

public class PriceServiceImpl implements PriceService {
  @Value("${dubbo.servicecomb.test.configuration:hello}")
  private String configuration;

  @Value("${dubbo.servicecomb.test.configurationService:hello}")
  private String configurationService;

  @Override
  public String sayHello(String name) {
    if ("timeout".equals(name)) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return name;
  }

  @Override
  public CompletableFuture<String> sayHelloAsync(String name) {
    return CompletableFuture.completedFuture(sayHello(name));
  }

  @Override
  public String testConfiguration(String value) {
    return configuration;
  }

  @Override
  public String testConfigurationService(String value) {
    return configurationService;
  }
}
