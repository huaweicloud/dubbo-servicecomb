/*
 *
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.dubbo.common;

import java.util.Map;

public class SchemaInfo {
  private String schemaId;

  private Map<String, String> parameters;

  public String getSchemaId() {
    return schemaId;
  }

  public SchemaInfo setSchemaId(String schemaId) {
    this.schemaId = schemaId;
    return this;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public SchemaInfo setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
    return this;
  }
}
