/*
 *
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.Test;

public class AkSkRequestAuthHeaderProviderTest {

  @Test
  public void shouldNotResolveCredentialsWhenAuthenticationIsDisabled() {
    AkSkRequestAuthHeaderProvider provider = new AkSkRequestAuthHeaderProvider();

    assertThat(provider.authHeaders()).isEmpty();
  }

  @Test
  public void shouldCreateHeadersUsingDefaultCipher() {
    AkSkRequestAuthHeaderProvider provider = provider(DefaultCipher.CIPHER_NAME, "secret-key");

    Map<String, String> headers = provider.authHeaders();

    assertThat(headers)
        .containsEntry(AkSkRequestAuthHeaderProvider.X_SERVICE_AK, "access-key")
        .containsEntry(AkSkRequestAuthHeaderProvider.X_SERVICE_SHAAKSK,
            AkSkRequestAuthHeaderProvider.sha256Encode("secret-key", "access-key"))
        .containsEntry(AkSkRequestAuthHeaderProvider.X_SERVICE_PROJECT, "project")
        .hasSize(3);
  }

  @Test
  public void shouldKeepSecretThatIsAlreadyShaAkSkEncoded() {
    AkSkRequestAuthHeaderProvider provider = provider(ShaAKSKCipher.CIPHER_NAME, "encoded-secret");

    assertThat(provider.authHeaders())
        .containsEntry(AkSkRequestAuthHeaderProvider.X_SERVICE_SHAAKSK, "encoded-secret");
  }

  @Test
  public void shouldRejectUnknownCipherWhenAuthenticationIsEnabled() {
    AkSkRequestAuthHeaderProvider provider = provider("missing-cipher", "secret-key");

    assertThatThrownBy(provider::authHeaders)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("failed to find cipher named missing-cipher");
  }

  @Test
  public void shouldProduceKnownHmacSha256Value() {
    assertThat(AkSkRequestAuthHeaderProvider.sha256Encode("key", "The quick brown fox jumps over the lazy dog"))
        .isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
  }

  private AkSkRequestAuthHeaderProvider provider(String cipher, String secretKey) {
    AkSkRequestAuthHeaderProvider provider = new AkSkRequestAuthHeaderProvider();
    provider.setEnabled(true);
    provider.setAccessKey("access-key");
    provider.setSecretKey(secretKey);
    provider.setCipher(cipher);
    provider.setProject("project");
    return provider;
  }
}
