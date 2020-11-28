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

package com.huaweicloud.dubbo.governance.properties;

import com.huaweicloud.dubbo.governance.policy.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RetryProperties implements GovProperties<RetryPolicy> {

  Map<String, String> retry;

  @Autowired
  SerializeCache<RetryPolicy> cache = new SerializeCache<>();
  public RetryProperties () {
    retry = new HashMap<>();
    retry.put("xxx","rules:\n match: demo-retry.xx\nmaxAttempts: 3\nonSame: false\nretryOnResponseStatus: 502");
  }
  public Map<String, String> getRetry() {
    return retry;
  }

  public void setRetry(Map<String, String> retry) {
    this.retry = retry;
  }

  public Map<String, RetryPolicy> covert() {
    return cache.get(retry, RetryPolicy.class);
  }
}
