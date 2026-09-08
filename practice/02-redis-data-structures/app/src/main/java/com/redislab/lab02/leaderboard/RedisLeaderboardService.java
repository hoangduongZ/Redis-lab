package com.redislab.lab02.leaderboard;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * "Tam bang go co khe truot": ZSet luon o trang thai da sap xep san, nen Top 10
 * khong bao gio can sap xep - chi can nhin len dau bang. Khong dung @Cacheable
 * nua, thao tac thang bang ZSetOperations dung nhu giao an.
 */
@Service
public class RedisLeaderboardService {

	public static final String LEADERBOARD_KEY = "game_leaderboard";

	private final RedisTemplate<String, String> redisTemplate;

	public RedisLeaderboardService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void addScore(String playerName, double score) {
		redisTemplate.opsForZSet().add(LEADERBOARD_KEY, playerName, score);
	}

	public void addAll(Set<ZSetOperations.TypedTuple<String>> tuples) {
		redisTemplate.opsForZSet().add(LEADERBOARD_KEY, tuples);
	}

	public Set<ZSetOperations.TypedTuple<String>> getTop10() {
		return redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, 9);
	}

	/**
	 * Bonus nho: ZREVRANK tra loi ngay "toi dang hang thu may" ma khong can
	 * quet ai ca - dieu ma Postgres khong the lam voi chi phi tuong duong.
	 */
	public Long getRank(String playerName) {
		return redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, playerName);
	}

	public Double getScore(String playerName) {
		return redisTemplate.opsForZSet().score(LEADERBOARD_KEY, playerName);
	}
}
