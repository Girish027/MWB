package com.tfs.learningsystems.util.redis;

import com.tfs.learningsystems.config.AppConfig;
import com.tfs.learningsystems.ui.DataManagementManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

/**
 * initialize all necessary instances needed for Redis operations
 */
@Configuration
public class RedisConfig {

  @Qualifier("appConfig")
  @Autowired
  private AppConfig appConfig;

  @Autowired
  private DataManagementManager dataManagementManager;

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {

    String host = appConfig.getRedisHost();
    int port = appConfig.getRedisPort();

    JedisConnectionFactory factory = new JedisConnectionFactory();
    factory.setHostName(host);
    factory.setPort(port);
    return (factory);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {

    final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
    return template;
  }

  @Bean
  MessageListenerAdapter messageListener() {
    //
    // passing 'appConfig' directly would cause Spring failed to start.
    // bad Spring dependency, when you need it
    //
    String web2nlUrl = appConfig.getWeb2nlUrl().trim();
    String apiKey = appConfig.getWeb2nlApiKey().trim();
    String orionUrl = appConfig.getOrionURL().trim();
    String repositoryRoot = appConfig.getSharedRepositoryRoot();

    RedisMessageSubscriber rms = new RedisMessageSubscriber();
    rms.setApiKey(apiKey)
        .setOrionUrl(orionUrl)
        .setWeb2nlUrl(web2nlUrl)
        .setRepositoryRoot(repositoryRoot)
        .setDataManagementManager(dataManagementManager);
    return new MessageListenerAdapter(rms);
  }

  @Bean
  RedisMessageListenerContainer redisContainer() {

    final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(jedisConnectionFactory());
    container.addMessageListener(messageListener(), topic());
    return container;
  }

  @Bean
  MessagePublisher redisPublisher() {

    return new RedisMessagePublisher(redisTemplate(), topic());
  }

  @Bean
  ChannelTopic topic() {

    return new ChannelTopic("pubsub:model_test_queue");
  }
}
