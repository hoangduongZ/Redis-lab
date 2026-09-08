package com.redislab.lab02.playground;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * "Ong nuoc mot chieu": nhet email vao mot dau (LPUSH), rut ra xu ly o dau
 * kia (RPOP). Hang doi gui email dan dan, khong don u cung mot luc.
 */
@Service
public class EmailQueueService {

	private static final String QUEUE_KEY = "email_queue";

	private final RedisTemplate<String, String> redisTemplate;

	public EmailQueueService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Long enqueue(String email) {
		return redisTemplate.opsForList().leftPush(QUEUE_KEY, email);
	}

	public String dequeue() {
		return redisTemplate.opsForList().rightPop(QUEUE_KEY);
	}
}
