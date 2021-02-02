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

package com.huaweicloud.dubbo.dtm;

import java.util.Map;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(group = CommonConstants.CONSUMER)
public class DtmConsumerFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmConsumerFilter.class);

  @Override
  @SuppressWarnings("unchecked")
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    try {
      if (DtmConfig.getContextGetMethod() == null) {
        return invoker.invoke(invocation);
      }
      Object context = DtmConfig.getContextGetMethod().invoke(null);
      if (context instanceof Map) {
        invocation.getAttachments().putAll((Map<? extends String, ? extends String>) context);
      }
    } catch (Throwable e) {
      LOGGER.warn("Failed to add dtm consumer context: " + e.getMessage());
    }
    return invoker.invoke(invocation);
  }
}
