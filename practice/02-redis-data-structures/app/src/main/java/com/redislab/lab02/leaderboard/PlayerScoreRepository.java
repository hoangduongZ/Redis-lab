package com.redislab.lab02.leaderboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlayerScoreRepository extends JpaRepository<PlayerScore, Long> {

	/**
	 * Chinh la cau SQL "noi dau" trong giao an:
	 * SELECT player_name, score FROM leader_board ORDER BY score DESC LIMIT 10
	 * Vi khong co index tren "score", Postgres phai quet + sap xep tu dau moi lan goi.
	 */
	@Query(value = "SELECT * FROM player_score ORDER BY score DESC LIMIT 10", nativeQuery = true)
	List<PlayerScore> findTop10ByScoreDesc();
}
