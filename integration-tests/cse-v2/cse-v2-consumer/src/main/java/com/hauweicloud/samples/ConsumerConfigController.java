package com.hauweicloud.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerConfigController {

    @Autowired
    private Environment environment;

    @Autowired
    private ConsumerConfigurationProperties consumerConfigurationProperties;

    @GetMapping("/config")
    public String config(@RequestParam("key") String key) {
        return environment.getProperty(key);
    }

    @GetMapping("/foo")
    public String foo() {
        return consumerConfigurationProperties.getFoo();
    }

    @GetMapping("/bar")
    public String bar() {
        return consumerConfigurationProperties.getBar();
    }

    @GetMapping("/priority")
    public String priority() {
        return consumerConfigurationProperties.getPriority();
    }

    @GetMapping("/common")
    public String common() {
        return consumerConfigurationProperties.getCommon();
    }
}
