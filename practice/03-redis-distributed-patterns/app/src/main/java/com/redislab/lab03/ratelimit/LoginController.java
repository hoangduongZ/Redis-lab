package com.redislab.lab03.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
public class LoginController {

	@Value("${server.port}")
	private String instancePort;

	private final NaiveRateLimiterService naiveRateLimiterService;
	private final RedisRateLimiterService redisRateLimiterService;

	public LoginController(NaiveRateLimiterService naiveRateLimiterService,
			RedisRateLimiterService redisRateLimiterService) {
		this.naiveRateLimiterService = naiveRateLimiterService;
		this.redisRateLimiterService = redisRateLimiterService;
	}

	@PostMapping("/naive")
	public ResponseEntity<Map<String, Object>> loginNaive(@RequestParam String ip) {
		boolean allowed = naiveRateLimiterService.allow(ip);
		return respond(allowed, "bo dem rieng instance :" + instancePort);
	}

	@PostMapping("/redis")
	public ResponseEntity<Map<String, Object>> loginRedis(@RequestParam String ip) {
		boolean allowed = redisRateLimiterService.allow(ip);
		return respond(allowed, "bo dem dung chung qua Redis, xu ly boi instance :" + instancePort);
	}

	private ResponseEntity<Map<String, Object>> respond(boolean allowed, String note) {
		if (!allowed) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
					.body(Map.of("allowed", false, "note", note));
		}
		return ResponseEntity.ok(Map.of("allowed", true, "note", note));
	}
}
