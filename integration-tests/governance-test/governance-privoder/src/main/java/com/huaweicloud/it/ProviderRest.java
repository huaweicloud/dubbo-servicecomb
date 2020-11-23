package com.huaweicloud.it;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CompletableFuture;

@Path("/test")
public class ProviderRest{

  @GET
  @Path("/sayHello")
  @Produces({MediaType.APPLICATION_JSON})
  public String sayHello() {
    return "Hello World!";
  }

  @GET
  @Path("/sayHelloAsync")
  @Produces({MediaType.APPLICATION_JSON})
  public CompletableFuture<String> sayHelloAsync() {
    return CompletableFuture.completedFuture("Successful!");
  }

  @GET
  @Path("/testConfiguration")
  public String testConfiguration(String value) {
    return null;
  }

  @GET
  @Path("/testConfigurationService")
  public String testConfigurationService(String value) {
    return null;
  }
}
