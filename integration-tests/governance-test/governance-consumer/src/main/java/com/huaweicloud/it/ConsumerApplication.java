package com.huaweicloud.it;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConsumerApplication {
  public static void main(String args) throws Exception {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        "classpath*:spring/dubbo-provider.xml", "classpath*:spring/dubbo-servicecomb.xml");
    context.start();

//    ProviderService providerService = context.getBean("ProviderService", ProviderService.class);
//    PingService pingService = context.getBean("PingService", PingService.class);

//    while (true) {
//      try {
//      Thread.sleep(6000);
//      System.out.println(providerService.sayHello());
//      System.out.println(pingService.ping());
//      } catch (Exception e) {
//        System.out.println(e.getMessage());
//      }
//    }
  }
}
