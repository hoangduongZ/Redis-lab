package com.redislab.lab03.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Dung dung co che giao an mo ta: INCR + EXPIRE(~1s) tren cung 1 key theo IP.
 * Vi tat ca instance deu doc/ghi chung 1 key tren Redis, tong so request lot
 * qua luon dung bang nguong, bat ke request rai deu cho bao nhieu instance.
 */
@Service
public class RedisRateLimiterService {

	private static final int LIMIT_PER_SECOND = 5;

	private final StringRedisTemplate redisTemplate;

	public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public boolean allow(String ip) {
		long currentSecond = System.currentTimeMillis() / 1000;
		String key = "rate_limit:" + ip + ":" + currentSecond;

		Long count = redisTemplate.opsForValue().increment(key);
		if (count != null && count == 1L) {
			redisTemplate.expire(key, Duration.ofSeconds(2));
		}
		return count != null && count <= LIMIT_PER_SECOND;
	}
}
