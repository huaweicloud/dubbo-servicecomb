package com.huaweicloud.dubbo.discovery;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(group = CONSUMER, order = Integer.MIN_VALUE)
public class UpdateSubscriptionFilter implements Filter {
  private RegistrationListener registrationListener;

  public void setRegistrationListener(RegistrationListener registrationListener) {
    this.registrationListener = registrationListener;
  }

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    registrationListener.registerSubscriptionIfAbsent(invoker.getUrl());
    return invoker.invoke(invocation);
  }
}
