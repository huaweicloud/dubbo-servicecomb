package com.huaweicloud.dubbo.governance;

import com.huaweicloud.dubbo.governance.util.HeaderUtil;
import com.huaweicloud.dubbo.governance.client.track.RequestTrackContext;
import com.huaweicloud.dubbo.governance.marker.GovHttpRequest;
import com.huaweicloud.dubbo.governance.policy.Policy;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.Filter;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Activate(group = {PROVIDER})
public class RpcGovernanceFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RpcContext.class);

  private static final String RATE_LIMITING_POLICY_NAME = "RateLimitingPolicy";

  private static final String CIRCUIT_BREAKER_POLICY_NAME = "CircuitBreakerPolicy";

  private static final String BULKHEAD_POLICY_NAME = "BulkheadPolicy";


  private MatchersManager matchersManager = new MatchersManager();

  private GovManager govManager =new GovManager();

  private GovHttpRequest govHttpRequest ;

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
    HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
    if (request == null) {
      govHttpRequest = covertInvocation(invocation);
    } else {
      govHttpRequest = convert(request);
    }
    Map<String, Policy> policies = matchersManager.match(govHttpRequest);
    if (CollectionUtils.isEmpty(policies)) {
      return invoker.invoke(invocation);
    }
    RequestTrackContext.setPolicies(new ArrayList(policies.values()));
    try {
      Object result = govManager.processServer(RequestTrackContext.getPolicies(), ()-> invoker.invoke(invocation));
    } catch (Throwable th) {
      LOGGER.debug("request error, detail info print : {}", request);
      if (th instanceof RequestNotPermitted) {
        SendError(response,502,"rate limit!");
        LOGGER.warn("the request is rate limit by policy : {}",
            policies.get(RATE_LIMITING_POLICY_NAME));
      } else if (th instanceof CallNotPermittedException) {
        SendError(response,502,"circuitBreaker is open!");
        LOGGER.warn("circuitBreaker is open by policy : {}",
            policies.get(CIRCUIT_BREAKER_POLICY_NAME));
      } else if (th instanceof BulkheadFullException) {
        SendError(response,502,"bulkhead is full and does not permit further calls!");
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
            policies.get(BULKHEAD_POLICY_NAME));
      } else {
        try {
          throw th;
        } catch (Throwable throwable) {
          throwable.printStackTrace();
        }
      }
    } finally {
      RequestTrackContext.remove();
    }
    return invoker.invoke(invocation);
  }

  private GovHttpRequest convert(HttpServletRequest request) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(HeaderUtil.getHeaders(request));
    govHttpRequest.setMethod(request.getMethod());
    govHttpRequest.setUri(request.getRequestURI());
    return govHttpRequest;
  }

  private GovHttpRequest covertInvocation(Invocation invocation) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(invocation.getAttachments());
    govHttpRequest.setMethod(invocation.getMethodName());
    govHttpRequest.setUri(RpcContext.getContext().getUrl().getAddress());
    return govHttpRequest;
  }

  private void SendError(HttpServletResponse response, int StatusNum, String msg) {
    try {
      response.sendError(StatusNum,msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
