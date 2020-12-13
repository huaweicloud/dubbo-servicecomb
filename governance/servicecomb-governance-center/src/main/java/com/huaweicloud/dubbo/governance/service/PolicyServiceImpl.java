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

package com.huaweicloud.dubbo.governance.service;

import com.huaweicloud.dubbo.governance.policy.AbstractPolicy;
import com.huaweicloud.dubbo.governance.policy.Policy;
import com.huaweicloud.dubbo.governance.policy.RateLimitingPolicy;
import com.huaweicloud.dubbo.governance.properties.BulkheadProperties;
import com.huaweicloud.dubbo.governance.properties.CircuitBreakerProperties;
import com.huaweicloud.dubbo.governance.properties.GovProperties;
import com.huaweicloud.dubbo.governance.properties.RateLimitProperties;
import com.huaweicloud.dubbo.governance.properties.RetryProperties;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PolicyServiceImpl implements PolicyService {

  private static final String MATCH_NONE = "none";

  private List<GovProperties<? extends AbstractPolicy>> propertiesList;

  public PolicyServiceImpl() {
    propertiesList = new LinkedList<>();
    propertiesList.add(new RateLimitProperties());
    propertiesList.add(new BulkheadProperties());
    propertiesList.add(new CircuitBreakerProperties());
  }

  @Override
  public Map<String, Policy> getAllPolicies(List<String> mark) {
    if (CollectionUtils.isEmpty(mark)) {
      return null;
    }
    Map<String, Policy> policies = new HashMap<>();
    for (GovProperties<? extends AbstractPolicy> properties : propertiesList) {
      Policy policy = match(properties.covert(), mark);
      if (policy != null) {
        String name = policy.name();
        name = name.substring(name.lastIndexOf('.') + 1);
        policies.put(name, policy);
      }
    }
    return policies;
  }

  @Override
  public Policy getCustomPolicy(String kind, List<String> mark) {
    RetryProperties retryProperties = new RetryProperties();
    switch (kind) {
      case "Retry":
        return match(retryProperties.covert(), mark);
      default:
        return null;
    }
  }

  private <T extends AbstractPolicy> Policy match(Map<String, T> policies, List<String> mark) {
    AbstractPolicy policyResult;
    AbstractPolicy defaultPolicy = null;
    for (Entry<String, T> entry : policies.entrySet()) {
      if (entry.getValue().getRules().getMatch().equals(MATCH_NONE)) {
        defaultPolicy = entry.getValue();
        defaultPolicy.setName(entry.getKey());
      }
      if (entry.getValue().match(mark)) {
        policyResult = entry.getValue();
        policyResult.setName(entry.getKey());
        return policyResult;
      }
    }
    return defaultPolicy;
  }
}
