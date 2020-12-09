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

package com.huaweicloud.dubbo.governance;

import com.huaweicloud.dubbo.governance.marker.GovHttpRequest;
import com.huaweicloud.dubbo.governance.service.MatchersServiceImpl;
import com.huaweicloud.dubbo.governance.service.PolicyService;
import com.huaweicloud.dubbo.governance.policy.Policy;
import com.huaweicloud.dubbo.governance.service.MatchersService;
import com.huaweicloud.dubbo.governance.service.PolicyServiceImpl;

import java.util.List;
import java.util.Map;

public class MatchersManagerImpl implements MatchersManager{

  private static final String RETRY = "Retry";

  private MatchersService matchersService = new MatchersServiceImpl();

  private PolicyService policyService = new PolicyServiceImpl();

  @Override
  public Map<String, Policy> match(GovHttpRequest request) {
    /**
     * 1.获取该请求携带的markers
     */
    List<String> marks = matchersService.getMatchStr(request);
    /**
     * 2.通过 markers获取到所有的policy
     */
    return policyService.getAllPolicies(marks);
  }

  @Override
  public Policy matchRetry(GovHttpRequest request) {
    /**
     * 1.获取该请求携带的markers
     */
    List<String> marks = matchersService.getMatchStr(request);
    /**
     * 2.通过 markers获取到所有的policy
     */
    return policyService.getCustomPolicy(RETRY, marks);
  }
}
