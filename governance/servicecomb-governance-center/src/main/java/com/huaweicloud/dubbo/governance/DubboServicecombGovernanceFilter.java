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

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.ext.ServerRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;

@Activate(group = PROVIDER, order = -1000)
public class DubboServicecombGovernanceFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcContext.class);

  private RateLimitingHandler rateLimitingHandler;

  private CircuitBreakerHandler circuitBreakerHandler;

  private BulkheadHandler bulkheadHandler;

  private ServerRecoverPolicy<Object> serverRecoverPolicy;

  public void setRateLimitingHandler(RateLimitingHandler rateLimitingHandler) {
    this.rateLimitingHandler = rateLimitingHandler;
  }

  public void setCircuitBreakerHandler(CircuitBreakerHandler circuitBreakerHandler) {
    this.circuitBreakerHandler = circuitBreakerHandler;
  }

  public void setBulkheadHandler(BulkheadHandler bulkheadHandler) {
    this.bulkheadHandler = bulkheadHandler;
  }

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    GovernanceRequest governanceRequest = convert(invocation);

    CheckedFunction0<Result> next = () -> {
      Result result = invoker.invoke(invocation);
      if (result.hasException()) {
        throw result.getException();
      }
      return result;
    };
    DecorateCheckedSupplier<Result> dcs = Decorators.ofCheckedSupplier(next);

    try {
      DubboServicecombInvocationContext.setInvocationContext();

      addRateLimiting(dcs, governanceRequest);
      addBulkhead(dcs, governanceRequest);
      addCircuitBreaker(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable th) {
      throw processException(th, invoker, invocation);
    } finally {
      DubboServicecombInvocationContext.removeInvocationContext();
    }
  }

  // Extract the relevant parameters of the RPC call
  private GovernanceRequest convert(Invocation invocation) {
    GovernanceRequest govHttpRequest = new GovernanceRequest();
    govHttpRequest.setHeaders(invocation.getAttachments());
    govHttpRequest.setMethod("POST");
    govHttpRequest.setUri(invocation.getTargetServiceUniqueName() + "." + invocation.getMethodName());
    return govHttpRequest;
  }

  // Unified processing exceptions
  private RpcException processException(Throwable th, Invoker<?> invoker, Invocation invocation) {
    if (th instanceof RequestNotPermitted) {
      LOGGER.warn("the request is rate limit by policy : {}",
          th.getMessage());
      return this.wrapError(invoker, invocation, "rate limited.", 429);
    } else if (th instanceof CallNotPermittedException) {
      LOGGER.warn("circuitBreaker is open by policy : {}",
          th.getMessage());
      return this.wrapError(invoker, invocation, "circuitBreaker is open.", 429);
    } else if (th instanceof BulkheadFullException) {
      LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
          th.getMessage());
      return this.wrapError(invoker, invocation, "bulkhead is full and does not permit further calls.", 429);
    } else {
      LOGGER.error("", th);
      return new RpcException(th);
    }
  }

  // crate new RpcException, in order to make next service can catch this type error
  private RpcException wrapError(Invoker<?> invoker, Invocation invocation, String msg, int code) {
    return new RpcException(code,
        "Failed to invoke service " +
            invoker.getInterface().getName() +
            "." + invocation.getMethodName() +
            ":" + msg
    );
  }

  private void addBulkhead(DecorateCheckedSupplier<Result> dcs, GovernanceRequest request) {
    Bulkhead bulkhead = bulkheadHandler.getActuator(request);
    if (bulkhead != null) {
      dcs.withBulkhead(bulkhead);
    }
  }

  private void addCircuitBreaker(DecorateCheckedSupplier<Result> dcs, GovernanceRequest request) {
    CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(request);
    if (circuitBreaker != null) {
      dcs.withCircuitBreaker(circuitBreaker);
    }
  }

  private void addRateLimiting(DecorateCheckedSupplier<Result> dcs, GovernanceRequest request) {
    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    if (rateLimiter != null) {
      dcs.withRateLimiter(rateLimiter);
    }
  }
}
