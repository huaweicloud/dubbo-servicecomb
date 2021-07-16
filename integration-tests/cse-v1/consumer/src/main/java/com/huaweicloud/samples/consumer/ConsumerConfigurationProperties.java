package com.huaweicloud.samples.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(value = "cse.v1.test")
public class ConsumerConfigurationProperties {

    private String foo;

    private String bar;

    private List<String> sequences;

    private List<ConfigModel> configModels;

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

    public List<String> getSequences() {
        return sequences;
    }

    public void setSequences(List<String> sequences) {
        this.sequences = sequences;
    }

    public List<ConfigModel> getConfigModels() {
        return configModels;
    }

    public void setConfigModels(List<ConfigModel> configModels) {
        this.configModels = configModels;
    }

    @Override
    public String toString() {
        return "ConsumerConfigurationProperties{" +
                "foo='" + foo + '\'' +
                ", bar='" + bar + '\'' +
                ", sequences=" + sequences +
                ", configModels=" + configModels +
                '}';
    }
}
