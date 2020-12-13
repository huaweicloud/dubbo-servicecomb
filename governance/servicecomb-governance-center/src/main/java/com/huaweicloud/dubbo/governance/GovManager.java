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

package com.huaweicloud.dubbo.governance;

import com.huaweicloud.dubbo.governance.handler.*;
import com.huaweicloud.dubbo.governance.handler.ext.ServerRecoverPolicy;
import com.huaweicloud.dubbo.governance.policy.Policy;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import org.apache.dubbo.common.extension.ExtensionLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GovManager {

  Map<String, AbstractGovHandler<?>> handlers;

  // 利用dubbo的SPI特点，进行运行时加载。也支持对 ServerRecoverPolicy进行扩展
  ServerRecoverPolicy<?> serverRecoverPolicy = ExtensionLoader.getExtensionLoader(ServerRecoverPolicy.class).getDefaultExtension();

  public GovManager() {
    handlers = new HashMap<>();
    handlers.put("GovRateLimiting", new RateLimitingHandler());
    handlers.put("GovCircuitBreaker", new CircuitBreakerHandler());
    handlers.put("GovBulkhead", new BulkheadHandler());
  }

  public Object processServer(List<Policy> policies, CheckedFunction0<?> supplier) {
    DecorateCheckedSupplier<?> ds = Decorators.ofCheckedSupplier(supplier);
    for (Policy policy : policies) {
      if (handlers.get(policy.handler()) == null ||
          handlers.get(policy.handler()).type() == HandlerType.CLIENT) {
        continue;
      }
       ds = handlers.get(policy.handler()).process(ds, policy);
    }
    Try<?> of = Try.of(ds.decorate());
    of.recover(throwable -> {
      if (serverRecoverPolicy == null) {
        throw (RuntimeException) throwable;
      } else {
        //用户自定义的降级策略
//        return serverRecoverPolicy.apply((Throwable) throwable);
        return null;
      }
    });
    return of.get();
  }

}
