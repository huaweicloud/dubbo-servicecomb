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

import com.huaweicloud.dubbo.governance.event.DynamicConfigListener;
import com.huaweicloud.dubbo.governance.marker.GovHttpRequest;
import com.huaweicloud.dubbo.governance.policy.Policy;
import com.huaweicloud.dubbo.governance.track.RequestTrackContext;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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

import java.util.ArrayList;
import java.util.Map;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Activate(group = PROVIDER, order = -1000)
public class RpcGovernanceFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcContext.class);

  private static final String RATE_LIMITING_MSG = " because the request is rate limit!";

  private static final String CIRCUIT_BREAKER_MSG = " because circuitBreaker is open!";

  private static final String BULKHEAD_MSG = " because bulkhead is full and does not permit further calls!";

  private MatchersManagerImpl matchersManagerImpl = new MatchersManagerImpl();

  private GovManager govManager = new GovManager();

  private Object result = null;

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    Map<String, Policy> policies = matchersManagerImpl.match(covertInvocation(invocation));
    if (CollectionUtils.isEmpty(policies)) {
      return invoker.invoke(invocation);
    }
    RequestTrackContext.setPolicies(new ArrayList<Policy>(policies.values()));
    try {
      result = govManager.processServer(RequestTrackContext.getPolicies(), ()-> invoker.invoke(invocation));
    } catch (Throwable th) {
      processException(th, invoker, invocation);
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
    govHttpRequest.setUri(invocation.getAttachments().get("path") + "." + invocation.getMethodName());
    return govHttpRequest;
  }

  // Unified processing exceptions
  private void processException(Throwable th, Invoker<?> invoker, Invocation invocation) {
    if (th instanceof RequestNotPermitted) {
      LOGGER.warn("the request is rate limit by policy : {}",
          DynamicConfigListener.getRateLimitingData());
      this.throwError(invoker, invocation, RATE_LIMITING_MSG, 429);
    } else if (th instanceof CallNotPermittedException) {
      LOGGER.warn("circuitBreaker is open by policy : {}",
          DynamicConfigListener.getCircuitBreakerData());
      this.throwError(invoker, invocation, CIRCUIT_BREAKER_MSG, 502);
    } else if (th instanceof BulkheadFullException) {
      LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
          DynamicConfigListener.getBulkheadData());
      this.throwError(invoker, invocation, BULKHEAD_MSG, 423);
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
            ":"+msg
    );
    throw rpcException;
  }
}
