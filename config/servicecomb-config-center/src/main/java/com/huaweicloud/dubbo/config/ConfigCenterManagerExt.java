package com.huaweicloud.dubbo.config;

import com.google.common.eventbus.EventBus;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.common.ConfigConverter;

import java.util.Map;

public class ConfigCenterManagerExt extends ConfigCenterManager {

    private Map<String,Object> lastConfiguration;

    public void setLastConfiguration(Map<String, Object> lastConfiguration) {
        this.lastConfiguration = lastConfiguration;
    }

    public ConfigCenterManagerExt(ConfigCenterClient configCenterClient, EventBus eventBus, ConfigConverter configConverter, Map<String,Object> lastConfiguration) {
        super(configCenterClient,eventBus,configConverter);
        this.lastConfiguration = lastConfiguration;
    }
}
