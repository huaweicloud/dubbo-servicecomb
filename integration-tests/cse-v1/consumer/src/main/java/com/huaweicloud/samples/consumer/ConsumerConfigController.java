package com.huaweicloud.samples.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConsumerConfigController {

    @Autowired
    private Environment environment;

    @Autowired
    private ConsumerConfigurationProperties consumerConfigurationProperties;

    @GetMapping("/config")
    public String config(@RequestParam("key") String key) {
        System.out.println(consumerConfigurationProperties);
        return environment.getProperty(key);
    }

    @GetMapping("/foo")
    public String foo() {
        System.out.println(consumerConfigurationProperties);
        return consumerConfigurationProperties.getFoo();
    }

    @GetMapping("/bar")
    public String bar() {
        System.out.println(consumerConfigurationProperties);
        return consumerConfigurationProperties.getBar();
    }

    @GetMapping("/sequences")
    public List<String> sequences() {
        System.out.println(consumerConfigurationProperties);
        return consumerConfigurationProperties.getSequences();
    }

    @GetMapping("/models")
    public List<ConfigModel> models() {
        System.out.println(consumerConfigurationProperties);
        return consumerConfigurationProperties.getConfigModels();
    }
}
