package com.redislab.lab02.playground;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * "Tu ngan keo": moi field (ten, tuoi, email) nam trong mot ngan rieng.
 * Sua mot ngan (HSET) khong dung cham den cac ngan con lai - khac han viec
 * phai doc/ghi lai ca mot chuoi JSON dai.
 */
@Service
public class PlayerProfileService {

	private final RedisTemplate<String, String> redisTemplate;

	public PlayerProfileService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private String key(String playerId) {
		return "profile:" + playerId;
	}

	public void saveFields(String playerId, Map<String, String> fields) {
		redisTemplate.opsForHash().putAll(key(playerId), fields);
	}

	public Map<Object, Object> getProfile(String playerId) {
		return redisTemplate.opsForHash().entries(key(playerId));
	}
}
