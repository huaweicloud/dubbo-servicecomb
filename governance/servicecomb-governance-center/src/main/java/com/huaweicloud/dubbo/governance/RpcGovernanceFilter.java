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

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

import java.util.ArrayList;
import java.util.Map;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.dubbo.governance.track.RequestTrackContext;
import com.huaweicloud.governance.GovManager;
import com.huaweicloud.governance.MatchersManager;
import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.properties.BulkheadProperties;
import com.huaweicloud.governance.properties.CircuitBreakerProperties;
import com.huaweicloud.governance.properties.RateLimitProperties;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@Activate(group = PROVIDER, order = -1000)
public class RpcGovernanceFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcContext.class);

  private MatchersManager matchersManager;

  private GovManager govManager;

  private Object result = null;

  public void setGovManager(GovManager govManager) {
    this.govManager = govManager;
  }

  // Auto wire spring been.
  public void setMatchersManager(MatchersManager matchersManager) {
    this.matchersManager = matchersManager;
  }

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    Map<String, Policy> policies = matchersManager.match(covertInvocation(invocation));
    if (CollectionUtils.isEmpty(policies)) {
      return invoker.invoke(invocation);
    }
    RequestTrackContext.setPolicies(new ArrayList<>(policies.values()));
    try {
      result = govManager.processServer(RequestTrackContext.getPolicies(), () -> invoker.invoke(invocation));
    } catch (Throwable th) {
      processException(th, invoker, invocation, policies);
    } finally {
      RequestTrackContext.remove();
    }
    return (Result) result;
  }

  // Extract the relevant parameters of the RPC call
  private GovHttpRequest covertInvocation(Invocation invocation) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(invocation.getAttachments());
    govHttpRequest.setMethod("POST");
    govHttpRequest.setUri(invocation.getTargetServiceUniqueName() + "." + invocation.getMethodName());
    return govHttpRequest;
  }

  // Unified processing exceptions
  private void processException(Throwable th, Invoker<?> invoker, Invocation invocation, Map<String, Policy> policies) {
    if (th instanceof RequestNotPermitted) {
      LOGGER.warn("the request is rate limit by policy : {}",
          policies.get(RateLimitProperties.class.getName()));
      this.throwError(invoker, invocation, "rate limited.", 429);
    } else if (th instanceof CallNotPermittedException) {
      LOGGER.warn("circuitBreaker is open by policy : {}",
          policies.get(CircuitBreakerProperties.class.getName()));
      this.throwError(invoker, invocation, "circuitBreaker is open.", 429);
    } else if (th instanceof BulkheadFullException) {
      LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
          policies.get(BulkheadProperties.class.getName()));
      this.throwError(invoker, invocation, "bulkhead is full and does not permit further calls.", 429);
    } else {
      try {
        throw th;
      } catch (Throwable throwable) {
        LOGGER.error("An error occurred when an exception was thrown : {}",
            throwable.getCause());
      }
    }
  }

  // crate new RpcException, in order to make next service can catch this type error
  private void throwError(Invoker<?> invoker, Invocation invocation, String msg, int code) {
    RpcException rpcException = new RpcException(code,
        "Failed to invoke service " +
            invoker.getInterface().getName() +
            "." + invocation.getMethodName() +
            ":" + msg
    );
    throw rpcException;
  }
}
