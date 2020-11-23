//package com.huaweicloud.dubbo.governance.ribbon;
//
//import com.huaweicloud.dubbo.governance.cache.LastInvokeServerCache;
//import com.netflix.loadbalancer.Server;
//import com.netflix.loadbalancer.ZoneAvoidanceRule;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Comparator;
//import java.util.List;
//
//public class ServiceCombLoadBalanceRule extends ZoneAvoidanceRule {
//
//  @Autowired
//  private List<RibbonServerFilter> list;
//
//  boolean isSorted = false;
//
//  public Server choose(Object key) {
//    List<Server> serverList = getLoadBalancer().getReachableServers();
//    if (!isSorted) {
//      list.sort(Comparator.comparingInt(RibbonServerFilter::order));
//      isSorted = true;
//    }
//    for (RibbonServerFilter filter : list) {
//      serverList = filter.filter(serverList);
//    }
//    Server lastInvoke = super.getPredicate().chooseRoundRobinAfterFiltering(serverList, key).orNull();
//    LastInvokeServerCache.setServer(lastInvoke);
//    return lastInvoke;
//  }
//}
