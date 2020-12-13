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

package com.huaweicloud.it.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;

import com.huaweicloud.it.price.PingService;
import com.huaweicloud.it.price.PriceService;

@SpringBootApplication
@ImportResource({"classpath*:spring/dubbo-provider.xml", "classpath*:spring/dubbo-servicecomb.xml"})
@Component
public class OrderApplication {
  private static ApplicationContext context;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    OrderApplication.context = context;
  }

  public static void main(String[] args) throws Exception {
    try {
      SpringApplication.run(OrderApplication.class);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    PriceService priceService = context.getBean("priceService", PriceService.class);
    PingService pingService = context.getBean("pingService", PingService.class);
    int num = 0;
    while (true) {
      try {
        Thread.sleep(3000);
        System.out.println(priceService.sayHello("===========================hello"));
        System.out.println(pingService.ping());
        System.out.println(priceService.sayRateLimit(++num));
        System.out.println(priceService.sayRateLimit(++num));
        System.out.println(priceService.sayHello("timeout"));
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}