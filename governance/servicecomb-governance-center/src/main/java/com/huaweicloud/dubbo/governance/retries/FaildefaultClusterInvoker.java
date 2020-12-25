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

package com.huaweicloud.dubbo.governance.retries;

import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_RETRIES;
import static org.apache.dubbo.common.constants.CommonConstants.RETRIES_KEY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.dubbo.rpc.support.RpcUtils;

import com.huaweicloud.governance.MatchersManager;
import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RetryPolicy;

public class FaildefaultClusterInvoker<T> extends AbstractClusterInvoker<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaildefaultClusterInvoker.class);

  private MatchersManager matchersManager;

  public FaildefaultClusterInvoker(Directory<T> directory, MatchersManager matchersManager) {
    super(directory);
    this.matchersManager = matchersManager;
  }

  private boolean onSame = false;//改成int区分onNext,先same

  private int retryTimes = 0;

  private String methodName;

  private RpcException rpcException = null;

  private Invoker<T> invoker = null;


  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance)
      throws RpcException {
    List<Invoker<T>> copyInvokers = invokers;
    checkInvokers(copyInvokers, invocation);
    //get retry times
    getRetryPolicy(invocation);
    // retry loop.
    List<Invoker<T>> invoked = new ArrayList<Invoker<T>>(copyInvokers.size()); // invoked invokers.
    Set<String> providers = new HashSet<String>(retryTimes);
    invoker = select(loadbalance, invocation, copyInvokers, invoked);
    for (int i = 0; i < retryTimes; i++) {
      //Reselect before retry to avoid a change of candidate `invokers`.
      //NOTE: if `invokers` changed, then `invoked` also lose accuracy.
      if (!onSame) {
        if (i > 0) {
          checkWhetherDestroyed();
          copyInvokers = list(invocation);
          checkInvokers(copyInvokers, invocation);
        }
        invoker = select(loadbalance, invocation, copyInvokers, invoked);
        invoked.add(invoker);
        RpcContext.getContext().setInvokers((List) invoked);
      }
      try {
        Result result = invoker.invoke(invocation);
        if (rpcException != null && LOGGER.isWarnEnabled()) {
          LOGGER.warn("Although retry the method " + methodName
              + " in the service " + getInterface().getName()
              + " was successful by the provider " + invoker.getUrl().getAddress()
              + ", but there have been failed providers " + providers
              + " (" + providers.size() + "/" + copyInvokers.size()
              + ") from the registry " + directory.getUrl().getAddress()
              + " on the consumer " + NetUtils.getLocalHost()
              + " using the dubbo version " + Version.getVersion() + ". Last error is: "
              + rpcException.getMessage(), rpcException);
        }
        return result;
      } catch (RpcException e) {
        if (e.isBiz()) { // biz exception.
          throw e;
        }
        rpcException = e;
      } catch (Throwable e) {
        rpcException = new RpcException(e.getMessage(), e);
      } finally {
        providers.add(invoker.getUrl().getAddress());
      }
    }
    throw new RpcException(rpcException.getCode(), "Failed to invoke the method "
        + this.methodName + " in the service " + getInterface().getName()
        + ". Tried " + this.retryTimes + " times of the providers " + providers
        + " (" + providers.size() + "/" + copyInvokers.size()
        + ") from the registry " + directory.getUrl().getAddress()
        + " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version "
        + Version.getVersion() + ". Last error is: "
        + rpcException.getMessage(), rpcException.getCause() != null ?
        rpcException.getCause() : rpcException);
  }

  private void getRetryPolicy(Invocation invocation) {
    this.methodName = RpcUtils.getMethodName(invocation);
    //if you want to make some configure changes, you can dynamic change here.
    Map<String, Policy> policies = matchersManager.match(covertInvocation(invocation, getUrl()));
    RetryPolicy retryPolicy = null;
    for (Policy p : policies.values()) {
      if (p instanceof RetryPolicy) {
        retryPolicy = (RetryPolicy) p;
        break;
      }
    }
    if (retryPolicy != null) {
      this.retryTimes = retryPolicy.getMaxAttempts() + 1;
      this.onSame = retryPolicy.isOnSame();
    } else {
      this.retryTimes = getUrl().getMethodParameter(methodName, RETRIES_KEY, DEFAULT_RETRIES) + 1;
    }
    // make RPC invoker at least one times.
    if (this.retryTimes <= 0) {
      this.retryTimes = 1;
    }
  }

  private GovHttpRequest covertInvocation(Invocation invocation, URL consumerUrl) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(consumerUrl.getParameters());
    govHttpRequest.setMethod("POST");
    govHttpRequest.setUri(invocation.getServiceName() + "." + invocation.getMethodName());
    return govHttpRequest;
  }
}
