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

package com.huaweicloud.dubbo.governance.policy;

/**
 * 默认只有信号量,不提供线程池模式
 **/
public class BulkheadPolicy extends AbstractPolicy {

  public static final int DEFAULT_MAX_CONCURRENT_CALLS = 1000;

  public static final int DEFAULT_MAX_WAIT_DURATION = 0;

  private Integer maxConcurrentCalls;

  private Integer maxWaitDuration;

  public Integer getMaxConcurrentCalls() {
    if (maxConcurrentCalls == null) {
      maxConcurrentCalls = DEFAULT_MAX_CONCURRENT_CALLS;
    }
    return maxConcurrentCalls;
  }

  public void setMaxConcurrentCalls(Integer maxConcurrentCalls) {
    this.maxConcurrentCalls = maxConcurrentCalls;
  }

  public Integer getMaxWaitDuration() {
    if (maxWaitDuration == null) {
      maxWaitDuration = DEFAULT_MAX_WAIT_DURATION;
    }
    return maxWaitDuration;
  }

  public void setMaxWaitDuration(Integer maxWaitDuration) {
    this.maxWaitDuration = maxWaitDuration;
  }

  @Override
  public String handler() {
    return "GovBulkhead";
  }

  @Override
  public String toString() {
    return "BulkheadPolicy{" +
        "maxConcurrentCalls=" + maxConcurrentCalls +
        ", maxWaitDuration=" + maxWaitDuration +
        '}';
  }
}
