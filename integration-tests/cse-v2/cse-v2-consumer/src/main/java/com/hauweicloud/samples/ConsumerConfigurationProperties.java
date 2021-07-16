package com.hauweicloud.samples;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("cse.v2.test")
public class ConsumerConfigurationProperties {
    private String foo;

    private String bar;

    private String priority;

    private String common;


    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    public String getPriority() {
        return priority;
    }

    public String getCommon() {
        return common;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCommon(String common) {
        this.common = common;
    }
}
