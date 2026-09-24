package com.redislab.lab03.ticket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Dung dung code mau giao an 03: setIfAbsent lam "chia khoa" dung chung qua
 * Redis. Du chay bao nhieu instance, chi dung 1 nguoi cuop duoc chia khoa tai
 * 1 thoi diem, nen khong the ban lo ve khi nhieu request toi cung luc.
 */
@Service
public class RedisLockTicketService {

	private static final String LOCK_KEY = "LOCK_TICKET_BLACKPINK";

	@Value("${server.port}")
	private String instancePort;

	private final StringRedisTemplate redisTemplate;

	public RedisLockTicketService(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public String buy(String userId) {
		Boolean isLockAcquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, userId, Duration.ofSeconds(10));

		if (isLockAcquired != null && isLockAcquired) {
			try {
				Thread.sleep(500);
				return "Thanh cong! Ve thuoc ve " + userId + " (xu ly boi instance :" + instancePort + ").";
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return "Loi he thong";
			} finally {
				redisTemplate.delete(LOCK_KEY);
			}
		}
		return "That bai. Ve dang duoc nguoi khac mua hoac da het (tu choi boi instance :" + instancePort + ").";
	}

	public void reset() {
		redisTemplate.delete(LOCK_KEY);
	}
}
