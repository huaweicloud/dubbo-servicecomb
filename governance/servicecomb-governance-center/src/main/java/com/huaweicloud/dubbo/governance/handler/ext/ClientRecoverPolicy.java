package com.huaweicloud.dubbo.governance.handler.ext;

public interface ClientRecoverPolicy<T> {

  T apply(Throwable th);
}
