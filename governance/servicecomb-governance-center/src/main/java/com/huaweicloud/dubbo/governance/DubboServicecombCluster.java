/*
 *
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
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

import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.dubbo.rpc.cluster.support.wrapper.AbstractCluster;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;

public class DubboServicecombCluster extends AbstractCluster {
  public final static String NAME = "dubbo-servicecomb";

  private RetryHandler retryHandler;

  private ClientRecoverPolicy<Object> clientRecoverPolicy;

  public void setRetryHandler(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  public void setClientRecoverPolicy(
      ClientRecoverPolicy<Object> clientRecoverPolicy) {
    this.clientRecoverPolicy = clientRecoverPolicy;
  }


  @Override
  public <T> AbstractClusterInvoker<T> doJoin(Directory<T> directory) throws RpcException {
    DubboServicecombClusterInvoker<T> invoker = new DubboServicecombClusterInvoker<>(directory);
    invoker.setRetryHandler(retryHandler);
    invoker.setClientRecoverPolicy(clientRecoverPolicy);
    return invoker;
  }
}
