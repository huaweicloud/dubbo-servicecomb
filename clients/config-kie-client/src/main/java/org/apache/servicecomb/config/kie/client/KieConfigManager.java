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

package org.apache.servicecomb.config.kie.client;

import com.google.common.eventbus.EventBus;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.http.client.task.AbstractTask;
import org.apache.servicecomb.http.client.task.Task;

public class KieConfigManager extends AbstractTask {
  private static long POLL_INTERVAL = 15000;

  private KieConfigOperation configKieClient;

  private final EventBus eventBus;

  private ConfigurationsRequest configurationsRequest;

  public KieConfigManager(KieConfigOperation configKieClient, EventBus eventBus) {
    super("config-center-configuration-task");
    this.configKieClient = configKieClient;
    this.eventBus = eventBus;
  }

  public void setConfigurationsRequest(ConfigurationsRequest configurationsRequest) {
    this.configurationsRequest = configurationsRequest;
  }

  public void startConfigKieManager() {
    this.startTask(new PollConfigurationTask());
  }

  class PollConfigurationTask implements Task {
    @Override
    public void execute() {
      ConfigurationsResponse response = configKieClient.queryConfigurations(configurationsRequest);
      if (response.isChanged()) {
        configurationsRequest.setRevision(response.getRevision());
        eventBus.post(new KieConfigChangedEvent(response.getConfigurations()));
      }
      startTask(new BackOffSleepTask(POLL_INTERVAL, new PollConfigurationTask()));
    }
  }
}
