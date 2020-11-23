package com.huaweicloud.it;

import java.util.concurrent.CompletableFuture;

public interface ProviderServiceRest {
  String sayHello();

  CompletableFuture<String> sayHelloAsync();

  String testConfiguration(String value);

  String testConfigurationService(String value);
}
