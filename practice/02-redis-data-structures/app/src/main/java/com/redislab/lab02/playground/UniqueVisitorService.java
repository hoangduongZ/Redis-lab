package com.redislab.lab02.playground;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * "Ro ky dieu": ném bao nhieu ID cung duoc, nhung ném trung ID da co thi
 * Redis tu bo qua. SADD + SCARD dem duoc so nguoi dung duy nhat moi ngay
 * ma khong can lo trung.
 */
@Service
public class UniqueVisitorService {

	private final RedisTemplate<String, String> redisTemplate;

	public UniqueVisitorService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private String key(String day) {
		return "visitors:" + day;
	}

	public Long recordVisit(String day, String userId) {
		return redisTemplate.opsForSet().add(key(day), userId);
	}

	public Long countUnique(String day) {
		return redisTemplate.opsForSet().size(key(day));
	}
}
