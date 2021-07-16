package com.huaweicloud.samples;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class ConsumerConfigTest {

    RestTemplate template = new RestTemplate();

    @Test
    public void testConfig() {
        String result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v1.test.foo", String.class);
        assertThat(result).isEqualTo("foo");
        result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v1.test.bar", String.class);
        assertThat(result).isEqualTo("bar");
    }

    @Test
    public void testFooBar() {
        String result = template.getForObject(Config.GATEWAY_URL + "/foo", String.class);
        assertThat(result).isEqualTo("foo");
        result = template.getForObject(Config.GATEWAY_URL + "/bar", String.class);
        assertThat(result).isEqualTo("bar");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSequences() {
        List<String> result = template.getForObject(Config.GATEWAY_URL + "/sequences", List.class);
        assertThat(result.toString()).isEqualTo("[s0, s1]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testModels() {
        List<Map<?, ?>> result = template.getForObject(Config.GATEWAY_URL + "/models", List.class);
        assertThat(result.toString()).isEqualTo("[{name=s1, index=2}, {name=s2, index=3}]");
    }

}
