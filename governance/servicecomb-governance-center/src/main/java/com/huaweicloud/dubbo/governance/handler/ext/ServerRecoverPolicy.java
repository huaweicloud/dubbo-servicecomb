package com.huaweicloud.dubbo.governance.handler.ext;

public interface ServerRecoverPolicy<T> {

  T apply(Throwable th);
}
