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

package com.huaweicloud.it.portal;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "/portal")
public class PortalController {
  @Autowired
  private RestTemplate restTemplate;

  @GetMapping(path = "testAll")
  public void testAll() {

    String result = restTemplate.getForObject("http://order-consumer/price/sayHello?name={name}", String.class, "hello world");
    Assert.assertEquals(result, "hello world");

    result = restTemplate.getForObject("http://order-consumer/price/sayHelloGeneric?name={name}", String.class, "hello world");
    Assert.assertEquals(result, "hello world");

    result = restTemplate.getForObject("http://order-consumer/price/sayHello?name={name}", String.class, "timeout");
    Assert.assertEquals(result, "timeout");

    result = restTemplate.getForObject("http://order-consumer/price/testConfiguration", String.class);
    Assert.assertEquals(result, "peizhi");

    result = restTemplate.getForObject("http://order-consumer/price/testConfigurationService", String.class);
    Assert.assertEquals(result, "peizhi_service");
  }
}
