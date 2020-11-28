package com.huaweicloud.it.price;

import java.util.concurrent.CompletableFuture;

public interface PriceService {
  String sayHello(String name);

  CompletableFuture<String> sayHelloAsync(String name);

  String testConfiguration(String value);

  String testConfigurationService(String value);
}
