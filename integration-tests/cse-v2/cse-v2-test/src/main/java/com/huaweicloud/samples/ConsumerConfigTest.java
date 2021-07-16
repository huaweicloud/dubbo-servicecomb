package com.huaweicloud.samples;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsumerConfigTest {

    RestTemplate template = new RestTemplate();

    @Test
    public void testConfig() {
        String result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.foo", String.class);
        assertThat(result).isEqualTo("foo");
        result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.bar", String.class);
        assertThat(result).isEqualTo("bar");
        result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.priority", String.class);
        assertThat(result).isEqualTo("v3");
        result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v2.test.common", String.class);
        assertThat(result).isEqualTo("common");
    }

    @Test
    public void testFooBar() {
        String result = template.getForObject(Config.GATEWAY_URL + "/bar", String.class);
        assertThat(result).isEqualTo("bar");
        result = template.getForObject(Config.GATEWAY_URL + "/foo", String.class);
        assertThat(result).isEqualTo("foo");
        result = template.getForObject(Config.GATEWAY_URL + "/priority", String.class);
        assertThat(result).isEqualTo("v3");
        result = template.getForObject(Config.GATEWAY_URL + "/common", String.class);
        assertThat(result).isEqualTo("common");
    }

}
