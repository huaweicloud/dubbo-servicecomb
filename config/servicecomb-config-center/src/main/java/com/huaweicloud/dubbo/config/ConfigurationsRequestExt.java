package com.huaweicloud.dubbo.config;

import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;

public class ConfigurationsRequestExt extends ConfigurationsRequest {

    private String application;

    private String serviceName;

    private String version;

    private String environment;

    public ConfigurationsRequestExt setApplication(String application) {
        this.application = application;
        return this;
    }

    public ConfigurationsRequestExt setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ConfigurationsRequestExt setVersion(String version) {
        this.version = version;
        return this;
    }

    public ConfigurationsRequestExt setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getApplication() {
        return application;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    public String getEnvironment() {
        return environment;
    }
}
