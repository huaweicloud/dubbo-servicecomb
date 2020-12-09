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

import com.huaweicloud.dubbo.governance.event.DynamicConfigListener;
import com.huaweicloud.dubbo.governance.policy.BulkheadPolicy;

import java.util.Map;

public class BulkheadProperties implements GovProperties<BulkheadPolicy> {

  Map<String, String> bulkhead;

  SerializeCache<BulkheadPolicy> cache =new SerializeCache<>();

  public Map<String, String> getBulkhead() {
    return bulkhead;
  }

  public void setBulkhead(Map<String, String> bulkhead) {
    this.bulkhead = bulkhead;
  }

  @Override
  public Map<String, BulkheadPolicy> covert() {
    bulkhead = DynamicConfigListener.loadData(DynamicConfigListener.getBulkheadData());
    return cache.get(bulkhead, BulkheadPolicy.class);
  }
}
