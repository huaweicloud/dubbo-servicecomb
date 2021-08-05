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

package com.huaweicloud.dubbo.common;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;

public class AkSkRequestAuthHeaderProvider implements AuthHeaderProvider {

  private boolean enabled;

  private String accessKey;

  private String secretKey;

  private String cipher;

  private String project;

  public static final String X_SERVICE_AK = "X-Service-AK";

  public static final String X_SERVICE_SHAAKSK = "X-Service-ShaAKSK";

  public static final String X_SERVICE_PROJECT = "X-Service-Project";

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
    String decodedSecretKey = new String(findCipher().decrypt(this.secretKey.toCharArray()));
    // ShaAKSKCipher 不解密, 认证的时候不处理；其他算法解密为 plain，需要 encode 为 ShaAKSKCipher 去认证。
    if (ShaAKSKCipher.CIPHER_NAME.equalsIgnoreCase(getCipher())) {
      return decodedSecretKey;
    } else {
      return sha256Encode(decodedSecretKey, getAccessKey());
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

  @Override
  public Map<String, String> authHeaders() {
    Map<String, String> headers = new HashMap<>();
    if (enabled) {
      headers.put(X_SERVICE_AK, this.getAccessKey());
      headers.put(X_SERVICE_SHAAKSK, this.getSecretKey());
      headers.put(X_SERVICE_PROJECT, this.getProject());
    }
    return headers;
  }

  private Cipher findCipher() {
    if (DefaultCipher.CIPHER_NAME.equals(getCipher())) {
      return DefaultCipher.getInstance();
    }

    List<Cipher> ciphers = SPIServiceUtils.getOrLoadSortedService(Cipher.class);
    return ciphers.stream().filter(c -> c.name().equals(getCipher())).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + getCipher()));
  }

  public static String sha256Encode(String key, String data) {
    try {
      Mac sha256HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
          "HmacSHA256");
      sha256HMAC.init(secretKey);
      return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalArgumentException("Can not encode ak sk. Please check the value is correct.", e);
    }
  }

}
