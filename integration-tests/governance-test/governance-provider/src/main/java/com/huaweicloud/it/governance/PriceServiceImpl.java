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

package com.huaweicloud.it.governance;


import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.QueryParam;

public class PriceServiceImpl implements PriceService {

  private  int count = 0;

  @Override
  public String sayHello(String name) {
    return name + ": rate Limiting!";
  }

  @Override
  public String sayRetry(@QueryParam("name") String name) {
    count++;
    if (count%3 ==0) {
      return name + ": Retry!";
    } else {
      try {
        Thread.sleep(3000);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
    return null;
  }

}
