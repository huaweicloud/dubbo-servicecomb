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

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.huaweicloud.it.price.PriceService;

@Path("/price")
public class PriceServiceRestImpl implements PriceService {
  @Autowired
  @Qualifier("priceService")
  private PriceService priceService;

  @Override
  @GET
  @Path("/sayHello")
  @Produces({MediaType.APPLICATION_JSON})
  public String sayHello(@QueryParam("name") String name) {
    return priceService.sayHello(name);
  }

  // dubbo do not support CompletableFuture, this example can not work
  @Override
  @GET
  @Path("/sayHelloAsync")
  @Produces({MediaType.APPLICATION_JSON})
  public CompletableFuture<String> sayHelloAsync(@QueryParam("name") String name) {
    return CompletableFuture.completedFuture(name);
  }

  @Override
  @GET
  @Path("/testConfiguration")
  @Produces({MediaType.APPLICATION_JSON})
  public String testConfiguration(String value) {
    return priceService.testConfiguration(value);
  }

  @Override
  @GET
  @Path("/testConfigurationService")
  @Produces({MediaType.APPLICATION_JSON})
  public String testConfigurationService(String value) {
    return priceService.testConfigurationService(value);
  }
}
