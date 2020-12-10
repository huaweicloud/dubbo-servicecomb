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

package com.huaweicloud.dubbo.governance.event;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.dubbo.common.EventManager;
import com.huaweicloud.dubbo.common.MatchDataChangeEvent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Map;

public class DynamicConfigListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamicConfigListener.class);

  private static final Yaml SAFE_PARSER = new Yaml(new SafeConstructor());

  public static String policyData = null;

  public static String retryData = null;

  public static String circuitBreakerData = null;

  public static String rateLimitingData = null;

  public static String bulkheadData = null;

  public DynamicConfigListener() {
    EventManager.register(this);
  }

  @Subscribe
  public void onDynamicConfigurationListener(MatchDataChangeEvent event) {
    refreshMatchData(event.getConfigurations());
  }

  //refresh governance rules
  private void refreshMatchData(Map<String, Object> configurations) {
    policyData = (String) configurations.get(MatchDataChangeEvent.MATCH_POLICY_KEY);
    rateLimitingData = (String) configurations.get(MatchDataChangeEvent.MATCH_RAMTELING_KEY);
    retryData = (String) configurations.get(MatchDataChangeEvent.MATCH_RETRY_KEY);
    circuitBreakerData = (String) configurations.get(MatchDataChangeEvent.MATCH_CIRCUITBREAKER_KEY);
    bulkheadData = (String) configurations.get(MatchDataChangeEvent.MATCH_BULKHEAD__KEY);
    LOGGER.info("refresh governance rules success!");
  }

  //load governance rules
  public static Map<String, String> loadData(String data) {
    if (StringUtils.isEmpty(data)) {
      return null;
    }
    Map<String, String> result = SAFE_PARSER.load(data);
    return result;
  }

  public static String getPolicyData() {
    return policyData;
  }

  public static String getRetryData() {
    return retryData;
  }

  public static String getCircuitBreakerData() {
    return circuitBreakerData;
  }

  public static String getRateLimitingData() {
    return rateLimitingData;
  }

  public static String getBulkheadData() {
    return bulkheadData;
  }
}
