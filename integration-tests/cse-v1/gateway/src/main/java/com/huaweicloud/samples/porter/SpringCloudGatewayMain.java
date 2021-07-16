package com.huaweicloud.samples.porter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCloudGatewayMain {

    public static void main(String[] args) throws Exception {
        try {
            SpringApplication.run(SpringCloudGatewayMain.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
