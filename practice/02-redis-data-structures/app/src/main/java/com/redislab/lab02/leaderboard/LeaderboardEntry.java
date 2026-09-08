package com.redislab.lab02.leaderboard;

/**
 * Dang tra ve chung cho ca hai phia (Postgres va Redis) de so sanh cong bang
 * tren cung mot hinh dang JSON.
 */
public record LeaderboardEntry(String playerName, double score) {
}
