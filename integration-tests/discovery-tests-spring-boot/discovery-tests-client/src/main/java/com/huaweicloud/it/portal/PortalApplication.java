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

package com.huaweicloud.it.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Component
public class PortalApplication {
  private static PortalController portalController;

  @LoadBalanced
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate;
  }

  @Autowired
  void setPortalController(PortalController portalController) {
    PortalApplication.portalController = portalController;
  }


  public static void main(String[] args) throws Exception {
    try {
      SpringApplication.run(PortalApplication.class);
    } catch (Throwable e) {
      e.printStackTrace();
    }

    Thread.sleep(3000); // TODO: 尽可能规避 spring cloud huawei 的 bug, 需要等待新版本

    System.out.println("running all test cases");
    portalController.testAll();
    System.out.println("running all test cases successfully");
  }
}
