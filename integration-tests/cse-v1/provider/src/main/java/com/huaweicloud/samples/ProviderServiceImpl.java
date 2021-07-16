package com.huaweicloud.samples;


import com.huaweicloud.api.ProviderService;

public class ProviderServiceImpl implements ProviderService {

    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
