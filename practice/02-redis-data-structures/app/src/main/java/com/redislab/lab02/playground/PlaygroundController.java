package com.redislab.lab02.playground;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlaygroundController {

	private final EmailQueueService emailQueueService;
	private final UniqueVisitorService uniqueVisitorService;
	private final PlayerProfileService playerProfileService;

	public PlaygroundController(EmailQueueService emailQueueService, UniqueVisitorService uniqueVisitorService,
			PlayerProfileService playerProfileService) {
		this.emailQueueService = emailQueueService;
		this.uniqueVisitorService = uniqueVisitorService;
		this.playerProfileService = playerProfileService;
	}

	@PostMapping("/queue/email")
	public Map<String, Object> enqueueEmail(@RequestParam String email) {
		Long queueSize = emailQueueService.enqueue(email);
		return Map.of("queued", email, "queueSize", queueSize);
	}

	@GetMapping("/queue/email/next")
	public Map<String, Object> dequeueEmail() {
		String email = emailQueueService.dequeue();
		if (email == null) {
			return Map.of("message", "Hang doi rong");
		}
		return Map.of("processing", email);
	}

	@PostMapping("/visitors/{day}")
	public Map<String, Object> recordVisit(@PathVariable String day, @RequestParam String userId) {
		Long added = uniqueVisitorService.recordVisit(day, userId);
		return Map.of("day", day, "userId", userId, "newUnique", added != null && added > 0);
	}

	@GetMapping("/visitors/{day}/count")
	public Map<String, Object> countVisitors(@PathVariable String day) {
		return Map.of("day", day, "uniqueVisitors", uniqueVisitorService.countUnique(day));
	}

	@PostMapping("/profiles/{playerId}")
	public Map<String, Object> saveProfile(@PathVariable String playerId, @RequestBody Map<String, String> fields) {
		playerProfileService.saveFields(playerId, fields);
		return Map.of("playerId", playerId, "savedFields", fields.keySet());
	}

	@GetMapping("/profiles/{playerId}")
	public Map<Object, Object> getProfile(@PathVariable String playerId) {
		return playerProfileService.getProfile(playerId);
	}
}
