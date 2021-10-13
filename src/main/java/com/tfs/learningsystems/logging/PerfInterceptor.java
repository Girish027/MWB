package com.tfs.learningsystems.logging;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerfInterceptor implements MethodInterceptor {

  private static ConcurrentHashMap<String, MethodStats> methodStats = new ConcurrentHashMap<String, MethodStats>();
  private static long statLogFrequency = 10;
  private static long methodWarningThreshold = 1000;

  public Object invoke(MethodInvocation method) throws Throwable {

    long start = System.currentTimeMillis();
    try {
      return method.proceed();
    } finally {
      updateStats(method.getMethod().getName(), (System.currentTimeMillis() - start));
    }
  }

  private void updateStats(String methodName, long elapsedTime) {

    MethodStats stats = methodStats.get(methodName);
    if (stats == null) {
      stats = new MethodStats(methodName);
      methodStats.put(methodName, stats);
    }
    stats.count++;
    stats.totalTime += elapsedTime;
    if (elapsedTime > stats.maxTime) {
      stats.maxTime = elapsedTime;
    }

    if (elapsedTime > methodWarningThreshold) {
      log.warn("method warning: " + methodName + "(), cnt = " + stats.count + ", lastTime = "
          + elapsedTime + ", maxTime = " + stats
          .maxTime);
    }

    if (stats.count % statLogFrequency == 0) {
      long avgTime = stats.totalTime / stats.count;
      long runningAvg = (stats.totalTime - stats.lastTotalTime) / statLogFrequency;
      log.debug(
          "method: " + methodName + "(), cnt = " + stats.count + ", lastTime = " + elapsedTime
              + ", avgTime = " + avgTime + ", " +
              "runningAvg = " + runningAvg + ", maxTime = " + stats.maxTime);
      stats.lastTotalTime = stats.totalTime;
      stats.count = 0;

    }
  }

  class MethodStats {

    public String methodName;
    public long count;
    public long totalTime;
    public long lastTotalTime;
    public long maxTime;

    public MethodStats(String methodName) {

      this.methodName = methodName;
    }
  }
}
