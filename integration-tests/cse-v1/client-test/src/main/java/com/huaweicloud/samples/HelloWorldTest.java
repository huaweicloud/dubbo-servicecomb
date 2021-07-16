package com.huaweicloud.samples;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;


public class HelloWorldTest {

    RestTemplate template = new RestTemplate();

    @Test
    public void testHelloWorld() {
        String result = template.getForObject(Config.GATEWAY_URL + "/sayHello?name=World", String.class);
        assertThat(result).isEqualTo("Hello World");
    }

}
