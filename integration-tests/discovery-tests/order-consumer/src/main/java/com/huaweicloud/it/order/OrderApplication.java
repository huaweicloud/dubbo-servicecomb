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

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.huaweicloud.it.price.PingService;
import com.huaweicloud.it.price.PriceService;

public class OrderApplication {
  public static void main(String[] args) throws Exception {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        "classpath*:spring/dubbo-provider.xml", "classpath*:spring/dubbo-servicecomb.xml");
    context.start();

    PriceService priceService = context.getBean("priceService", PriceService.class);
    PingService pingService = context.getBean("pingService", PingService.class);

    while (true) {
      try {
        Thread.sleep(3000);
        System.out.println(priceService.sayHello("===========================hello"));
        System.out.println(pingService.ping());
        System.out.println(priceService.sayHello("timeout"));
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }

//    System.in.read();
  }
}
