package com.redislab.lab02.leaderboard;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Day chinh la "noi dau" cua giao an: moi lan can Top 10, phai gom du 500,000
 * dong lai, so sanh, sap xep, roi moi cat lay 10 nguoi dung dau.
 */
@Service
public class PostgresLeaderboardService {

	private final PlayerScoreRepository playerScoreRepository;

	public PostgresLeaderboardService(PlayerScoreRepository playerScoreRepository) {
		this.playerScoreRepository = playerScoreRepository;
	}

	public List<PlayerScore> getTop10() {
		return playerScoreRepository.findTop10ByScoreDesc();
	}
}
