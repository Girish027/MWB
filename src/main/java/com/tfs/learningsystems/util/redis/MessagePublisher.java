package com.tfs.learningsystems.util.redis;

public interface MessagePublisher {

  void publish(final String message);
}