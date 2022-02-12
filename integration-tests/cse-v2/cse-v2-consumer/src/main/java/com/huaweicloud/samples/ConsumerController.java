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

package com.huaweicloud.samples;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.huaweicloud.api.ProviderService;


@Path("/")
public class ConsumerController implements IConsumerController{

  @Autowired
  @Qualifier("providerService")
  ProviderService providerService;

  @Autowired
  @Qualifier("providerServiceGeneric")
  GenericService providerServiceGeneric;

  @GET
  @Path("/sayHello")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public String sayHello(@QueryParam("name") String name) {
    return providerService.sayHello(name);
  }

  @GET
  @Path("/sayHelloGeneric")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public String sayHelloGeneric(@QueryParam("name") String name) {
    return providerServiceGeneric.$invoke("sayHello", new String[] { "java.lang.String" }, new Object[]{ name }).toString();
  }
}
