package com.redislab.lab02.leaderboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

	private final PostgresLeaderboardService postgresLeaderboardService;
	private final RedisLeaderboardService redisLeaderboardService;

	public LeaderboardController(PostgresLeaderboardService postgresLeaderboardService,
			RedisLeaderboardService redisLeaderboardService) {
		this.postgresLeaderboardService = postgresLeaderboardService;
		this.redisLeaderboardService = redisLeaderboardService;
	}

	@GetMapping("/postgres-leaderboard/top10")
	public Map<String, Object> postgresTop10() {
		long start = System.currentTimeMillis();
		List<LeaderboardEntry> results = postgresLeaderboardService.getTop10().stream()
				.map(p -> new LeaderboardEntry(p.getPlayerName(), p.getScore()))
				.collect(Collectors.toList());
		long elapsedMs = System.currentTimeMillis() - start;
		return Map.of("results", results, "elapsedMs", elapsedMs);
	}

	@PostMapping("/redis-leaderboard/scores")
	public Map<String, Object> addScore(@RequestParam String player, @RequestParam double score) {
		redisLeaderboardService.addScore(player, score);
		return Map.of("player", player, "score", score);
	}

	@GetMapping("/redis-leaderboard/top10")
	public Map<String, Object> redisTop10() {
		long start = System.currentTimeMillis();
		List<LeaderboardEntry> results = redisLeaderboardService.getTop10().stream()
				.map(t -> new LeaderboardEntry(t.getValue(), t.getScore() == null ? 0 : t.getScore()))
				.collect(Collectors.toList());
		long elapsedMs = System.currentTimeMillis() - start;
		return Map.of("results", results, "elapsedMs", elapsedMs);
	}

	@GetMapping("/redis-leaderboard/rank/{player}")
	public ResponseEntity<Map<String, Object>> rank(@PathVariable String player) {
		Long reverseRank = redisLeaderboardService.getRank(player);
		if (reverseRank == null) {
			return ResponseEntity.notFound().build();
		}
		Double score = redisLeaderboardService.getScore(player);
		return ResponseEntity.ok(Map.of("player", player, "rank", reverseRank + 1, "score", score));
	}
}
