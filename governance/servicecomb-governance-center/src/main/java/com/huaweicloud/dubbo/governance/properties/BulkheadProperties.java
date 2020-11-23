package com.huaweicloud.dubbo.governance.properties;

import com.huaweicloud.dubbo.governance.policy.BulkheadPolicy;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
//@ConfigurationProperties("servicecomb")
public class BulkheadProperties implements GovProperties<BulkheadPolicy> {

  Map<String, String> bulkhead;

  @Autowired
  SerializeCache<BulkheadPolicy> cache =new SerializeCache<>();

  public Map<String, String> getBulkhead() {
    return bulkhead;
  }

  public void setBulkhead(Map<String, String> bulkhead) {
    this.bulkhead = bulkhead;
  }

  @Override
  public Map<String, BulkheadPolicy> covert() {
    return cache.get(bulkhead, BulkheadPolicy.class);
  }
}
