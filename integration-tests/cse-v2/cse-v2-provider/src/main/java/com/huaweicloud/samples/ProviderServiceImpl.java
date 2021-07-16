package com.huaweicloud.samples;


import com.huaweicloud.api.Provider;

public class ProviderServiceImpl implements Provider {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
