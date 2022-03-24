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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.springframework.core.env.Environment;

public class AuthHeaderProviders {

  public static RequestAuthHeaderProvider getRequestAuthHeaderProvider(CommonConfiguration commonConfiguration,
      Environment environment) {
    List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
    authHeaderProviders.add(commonConfiguration.createAkSkRequestAuthHeaderProvider());
    authHeaderProviders.add(new RBACRequestAuthHeaderProvider(commonConfiguration, environment));
    return getRequestAuthHeaderProvider(authHeaderProviders);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(authHeaderProvider -> headers.putAll(authHeaderProvider.authHeaders()));
      return headers;
    };
  }
}
