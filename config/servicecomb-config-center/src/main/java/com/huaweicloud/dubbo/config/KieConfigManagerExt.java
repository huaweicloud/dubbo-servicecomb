package com.huaweicloud.dubbo.config;

import com.google.common.eventbus.EventBus;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.KieConfigOperation;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;

import java.util.Map;

public class KieConfigManagerExt extends KieConfigManager {

    private Map<String,Object> lastConfiguration;

    private ConfigurationsRequest configurationsRequest;

    public ConfigurationsRequest getConfigurationsRequest() {
        return configurationsRequest;
    }

    public KieConfigManager setConfigurationsRequest(ConfigurationsRequest configurationsRequest) {
        this.configurationsRequest = configurationsRequest;
        return this;
    }

    public void setLastConfiguration(Map<String, Object> lastConfiguration) {
        this.lastConfiguration = lastConfiguration;
    }

    public Map<String, Object> getLastConfiguration() {
        return lastConfiguration;
    }

    public KieConfigManagerExt(KieConfigOperation configKieClient, KieConfiguration kieConfiguration, ConfigConverter configConverter, EventBus eventBus, Map<String, Object> lastConfiguration) {
        super(configKieClient,eventBus,kieConfiguration,configConverter);
        this.lastConfiguration = lastConfiguration;
    }


}
