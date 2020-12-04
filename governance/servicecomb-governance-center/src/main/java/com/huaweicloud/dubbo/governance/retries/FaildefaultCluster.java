package com.huaweicloud.dubbo.governance.retries;

import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import org.apache.dubbo.rpc.cluster.support.wrapper.AbstractCluster;

public class FaildefaultCluster extends AbstractCluster {
  public final static String NAME = "faildefault";

  @Override
  public <T> AbstractClusterInvoker<T> doJoin(Directory<T> directory) throws RpcException {
    return new FaildefaultClusterInvoker<>(directory);
  }
}
