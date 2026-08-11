package com.redislab.lab01.config;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheConfig implements CachingConfigurer {

	/**
	 * Giu don gian: moi entry trong bang trang "books" song toi da 5 phut.
	 */
	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		RedisCacheConfiguration booksCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(5))
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

		return builder -> builder.withCacheConfiguration("books", booksCacheConfig);
	}
}
