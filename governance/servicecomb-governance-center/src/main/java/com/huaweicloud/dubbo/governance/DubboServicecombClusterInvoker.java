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

import java.util.List;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;

public class DubboServicecombClusterInvoker<T> extends AbstractClusterInvoker<T> {
  private RetryHandler retryHandler;

  private ClientRecoverPolicy<Object> clientRecoverPolicy;

  public void setRetryHandler(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  public void setClientRecoverPolicy(
      ClientRecoverPolicy<Object> clientRecoverPolicy) {
    this.clientRecoverPolicy = clientRecoverPolicy;
  }

  public DubboServicecombClusterInvoker(Directory<T> directory) {
    super(directory);
  }

  @Override
  public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance)
      throws RpcException {
    List<Invoker<T>> copyInvokers = invokers;
    checkInvokers(copyInvokers, invocation);

    GovernanceRequest governanceRequest = covertInvocation(invocation);

    CheckedFunction0<Result> next = () -> {
      checkInvokers(invokers, invocation);
      Invoker<T> invoker = select(loadbalance, invocation, invokers, null);
      Result result = invoker.invoke(invocation);
      if (result.hasException()) {
        throw result.getException();
      }
      return result;
    };

    DecorateCheckedSupplier<Result> dcs = Decorators.ofCheckedSupplier(next);

    try {
      DubboServicecombInvocationContext.setInvocationContext();

      addRetry(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      if (clientRecoverPolicy != null) {
        return (Result) clientRecoverPolicy.apply(e);
      }
      throw new RuntimeException(e);
    } finally {
      DubboServicecombInvocationContext.removeInvocationContext();
    }
  }

  private GovernanceRequest covertInvocation(Invocation invocation) {
    GovernanceRequest govHttpRequest = new GovernanceRequest();
    govHttpRequest.setHeaders(invocation.getAttachments());
    govHttpRequest.setMethod("POST");
    govHttpRequest.setUri(invocation.getServiceName() + "." + invocation.getMethodName());
    return govHttpRequest;
  }

  private void addRetry(DecorateCheckedSupplier<Result> dcs, GovernanceRequest request) {
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      dcs.withRetry(retry);
    }
  }
}
