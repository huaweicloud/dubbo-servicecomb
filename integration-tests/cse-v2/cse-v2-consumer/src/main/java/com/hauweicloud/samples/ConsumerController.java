package com.hauweicloud.samples;

import com.huaweicloud.api.Provider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ConsumerController {

    @DubboReference(url = "dubbo://localhost:20880")
    Provider provider;

    @GetMapping("/sayHello")
    public String sayHello(@RequestParam("name") String name) {
        return provider.sayHello(name);
    }

}
