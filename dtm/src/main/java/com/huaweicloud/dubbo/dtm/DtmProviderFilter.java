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

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(group = CommonConstants.PROVIDER)
public class DtmProviderFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmProviderFilter.class);

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    try {
      String traceId = invocation.getAttachments().get(DtmConfig.TRACE_ID_NAME);
      if (!StringUtils.isEmpty(traceId)) {
        invocation.getAttachments().put(DtmConfig.DTM_TRACE_ID_KEY, traceId);
      }
      DtmConfig.getContextSetMethod().invoke(null, invocation.getAttachments());
    } catch (Throwable e) {
      LOGGER.warn("Failed to add dtm producer context", e);
    }
    return invoker.invoke(invocation);
  }
}
