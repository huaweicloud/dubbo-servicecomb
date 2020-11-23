package com.huaweicloud.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Generated;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletableFuture;

@Path("/")
public class ProviderServiceRestImpl implements ProviderService{
  @Autowired
  @Qualifier("ProviderService")
  private ProviderService providerService;

  @Override
  @GET
  @Path("/sayHello")
  @Produces({MediaType.APPLICATION_JSON})
  public String sayHello() {
    return "Hello World!";
  }

  @Override
  @GET
  @Path("/sayHelloAsync")
  @Produces({MediaType.APPLICATION_JSON})
  public CompletableFuture<String> sayHelloAsync() {
    return CompletableFuture.completedFuture("Successful!");
  }

  @Override
  @GET
  @Path("/testConfiguration")
  public String testConfiguration(String value) {
    return null;
  }

  @Override
  @GET
  @Path("/testConfigurationService")
  public String testConfigurationService(String value) {
    return null;
  }
}
