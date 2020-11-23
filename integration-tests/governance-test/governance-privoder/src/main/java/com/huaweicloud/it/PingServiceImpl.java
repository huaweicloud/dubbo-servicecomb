package com.huaweicloud.it;

import com.huaweicloud.it.price.PingService;

public class PingServiceImpl implements PingService {

  @Override
  public boolean ping() {
    return true;
  }
}
