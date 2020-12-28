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

import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.rpc.RpcException;

public class GovernanceServiceImpl implements GovernanceService {
  private Map<String, Integer> retryTimes = new HashMap<>();

  @Override
  public String sayHello() {
    return "Hello world!";
  }

  @Override
  public String retry(String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "mocked timeout error";
  }

  @Override
  public String circuitBreaker() {
    throw new RuntimeException("circuitBreaker by provider.");
  }
}
