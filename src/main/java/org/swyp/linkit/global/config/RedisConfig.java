package org.swyp.linkit.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.swyp.linkit.domain.chat.redis.RedisChatSubscriber;
import org.swyp.linkit.domain.notification.redis.RedisNotificationSubscriber;

@Profile("!test")
@Configuration
public class RedisConfig {

    private static final String CHAT_CHANNEL_PATTERN = "chat:room:*";
    private static final String NOTIFICATION_CHANNEL_PATTERN = "notification:user:*";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisChatSubscriber redisChatSubscriber,
            RedisNotificationSubscriber redisNotificationSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisChatSubscriber, new PatternTopic(CHAT_CHANNEL_PATTERN));
        container.addMessageListener(redisNotificationSubscriber, new PatternTopic(NOTIFICATION_CHANNEL_PATTERN));
        return container;
    }
}