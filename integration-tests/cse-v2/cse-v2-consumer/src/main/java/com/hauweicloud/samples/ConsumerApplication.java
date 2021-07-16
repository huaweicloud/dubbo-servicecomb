package com.hauweicloud.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:spring/dubbo-consumer.xml","classpath*:spring/dubbo-servicecomb.xml"})
public class ConsumerApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ConsumerApplication.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
