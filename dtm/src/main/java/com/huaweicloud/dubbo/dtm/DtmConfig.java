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

import java.lang.reflect.Method;

import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

public class DtmConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmConfig.class);

  private static final String DTM_CONTEXT_CLASS_NAME = "com.huawei.middleware.dtm.client.context.DTMContext";

  private static final String DTM_EXPORT_METHOD = "getContextData";

  private static final String DTM_IMPORT_METHOD = "setContextData";

  public static final String DTM_TRACE_ID_KEY = "X-Dtm-Trace-Id-Key";

  public static final String TRACE_ID_NAME = "X-B3-TraceId";

  private static Method contextGetMethod;

  private static Method contextSetMethod;

  static {
    try {
      Class<?> clazz = Class.forName(DtmConfig.DTM_CONTEXT_CLASS_NAME);
      contextGetMethod = clazz.getMethod(DtmConfig.DTM_EXPORT_METHOD);
      contextSetMethod = clazz.getMethod(DtmConfig.DTM_IMPORT_METHOD);
    } catch (Throwable e) {
      LOGGER.warn("Failed to create DTMContext. Dtm client libraries must included. Cause: " + e.getMessage());
    }
  }

  public static Method getContextGetMethod() {
    return contextGetMethod;
  }

  public static Method getContextSetMethod() {
    return contextSetMethod;
  }
}
