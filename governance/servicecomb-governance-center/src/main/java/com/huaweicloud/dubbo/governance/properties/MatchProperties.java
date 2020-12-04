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

import com.huaweicloud.dubbo.governance.marker.TrafficMarker;

import java.util.HashMap;
import java.util.Map;

public class MatchProperties {

  Map<String, String> match;

  SerializeCache<TrafficMarker> cache = new SerializeCache<>();

  public MatchProperties () {
    match = new HashMap<>();
    match.put("demo-rateLimiting", "matches:\n - apiPath:\n     contains: \"sayHello\"\n   name: xx\n");
    match.put("demo-retry", "matches:\n - apiPath:\n     contains: \"sayHello\"\n   name: xx\n");

  }

  public Map<String, String> getMatch() {
    return match;
  }

  public void setMatch(Map<String, String> match) {
    this.match = match;
  }

  public Map<String, TrafficMarker> covert() {
    return cache.get(match, TrafficMarker.class);
  }
}
