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

package com.huaweicloud.dubbo.discovery;

import org.springframework.context.ApplicationEvent;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.NotifyListener;

public class NewSubscriberEvent extends ApplicationEvent {
  private static final long serialVersionUID = 1L;

  private final URL url;

  private final NotifyListener notifyListener;

  public NewSubscriberEvent(URL url, NotifyListener notifyListener) {
    super(url);
    this.url = url;
    this.notifyListener = notifyListener;
  }

  public URL getUrl() {
    return url;
  }

  public NotifyListener getNotifyListener() {
    return notifyListener;
  }
}
