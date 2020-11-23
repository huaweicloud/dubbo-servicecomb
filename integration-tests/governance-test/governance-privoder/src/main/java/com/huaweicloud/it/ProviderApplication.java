package com.huaweicloud.it;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProviderApplication {
  public static void main(String[] args) throws Exception {
    try {
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/dubbo-provider.xml",
          "classpath*:spring/dubbo-servicecomb.xml");
      context.start();
      System.in.read();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
