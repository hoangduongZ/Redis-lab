package com.redislab.lab03.ticket;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

	private final NaiveTicketService naiveTicketService;
	private final RedisLockTicketService redisLockTicketService;

	public TicketController(NaiveTicketService naiveTicketService, RedisLockTicketService redisLockTicketService) {
		this.naiveTicketService = naiveTicketService;
		this.redisLockTicketService = redisLockTicketService;
	}

	@PostMapping("/naive-ticket/buy")
	public Map<String, Object> buyNaive(@RequestParam String userId) {
		return Map.of("result", naiveTicketService.buy(userId));
	}

	@PostMapping("/redis-ticket/buy")
	public Map<String, Object> buyRedis(@RequestParam String userId) {
		return Map.of("result", redisLockTicketService.buy(userId));
	}

	@PostMapping("/ticket/reset")
	public Map<String, Object> reset() {
		naiveTicketService.reset();
		redisLockTicketService.reset();
		return Map.of("message", "Da reset ve, san sang thi nghiem lai.");
	}
}
