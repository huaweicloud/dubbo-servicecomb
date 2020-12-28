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

package org.apache.servicecomb.http.client.auth;

import org.apache.servicecomb.foundation.auth.SignRequest;
import org.apache.servicecomb.http.client.common.HttpUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultRequestAuthHeaderProvider implements RequestAuthHeaderProvider {

  private boolean enabled;

  private String accessKey;

  private String secretKey;

  private String cipher;

  private String project;

  public static  final String X_SERVICE_AK = "X-Service-AK";

  public static  final String X_SERVICE_SHAAKSK = "X-Service-ShaAKSK";

  public static  final String X_SERVICE_PROJECT = "X-Service-Project";

  @Override
  public Map<String, String> loadAuthHeader(SignRequest signRequest) {
    return this.getHeaders();
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    if ("ShaAKSKCipher".equalsIgnoreCase(this.cipher)) {
      return this.secretKey;
    }
    try {
      return HttpUtils.sha256Encode(this.secretKey, this.accessKey);
    } catch (Exception e) {
      throw new IllegalArgumentException("not able to encode ak sk.", e);
    }
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getCipher() {
    return cipher;
  }

  public void setCipher(String cipher) {
    this.cipher = cipher;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<>();
    if (enabled) {
      headers.put(X_SERVICE_AK, this.getAccessKey());
      headers.put(X_SERVICE_SHAAKSK, this.getSecretKey());
      headers.put(X_SERVICE_PROJECT, this.getProject());
    }
    return headers;
  }
}
