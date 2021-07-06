package com.huaweicloud.dubbo.config;

import org.apache.servicecomb.config.kie.client.model.KieAddressManager;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KieAddressManagerExt extends KieAddressManager {

    private final Properties properties;

    private final Map<String, String> configKey;

    public Properties getProperties() {
        return properties;
    }

    public Map<String, String> getConfigKey() {
        return configKey;
    }

    public KieAddressManagerExt(Properties properties,List<String> addresses,Map<String, String> configKey) {
        super(addresses);
        this.properties = properties;
        this.configKey = configKey;
    }
}
