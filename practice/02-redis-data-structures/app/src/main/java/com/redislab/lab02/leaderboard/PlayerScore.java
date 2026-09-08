package com.redislab.lab02.leaderboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Co tinh KHONG danh index cho cot "score" - dung tinh than giao an, de
 * ORDER BY score DESC phai quet va sap xep that su, khong duoc "ăn gian".
 */
@Entity
@Table(name = "player_score")
public class PlayerScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "player_name", nullable = false)
	private String playerName;

	@Column(name = "score", nullable = false)
	private double score;

	protected PlayerScore() {
	}

	public PlayerScore(String playerName, double score) {
		this.playerName = playerName;
		this.score = score;
	}

	public Long getId() {
		return id;
	}

	public String getPlayerName() {
		return playerName;
	}

	public double getScore() {
		return score;
	}
}
