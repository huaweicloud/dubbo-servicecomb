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

package com.huaweicloud.samples.consumer;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@Path("/")
public class ConsumerConfigController implements IConsumerConfigController {

  @Autowired
  private Environment environment;

  @Autowired
  private ConsumerConfigurationProperties consumerConfigurationProperties;

  @GET
  @Path("/config")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public String config(@QueryParam("key") String key) {
    return environment.getProperty(key);
  }

  @GET
  @Path("/foo")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public String foo() {
    return consumerConfigurationProperties.getFoo();
  }

  @GET
  @Path("/bar")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public String bar() {
    return consumerConfigurationProperties.getBar();
  }

  @GET
  @Path("/sequences")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public List<String> sequences() {
    return consumerConfigurationProperties.getSequences();
  }

  @GET
  @Path("/models")
  @Produces({MediaType.APPLICATION_JSON})
  @Override
  public List<ConfigModel> models() {
    return consumerConfigurationProperties.getConfigModels();
  }
}
