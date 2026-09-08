package com.redislab.lab02.leaderboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sinh N nguoi choi ngau nhien roi nap CUNG mot du lieu do vao ca Postgres
 * lan Redis ZSet - de phep so sanh "ai nhanh hon" la cong bang, khac cau
 * truc luu tru chu khong khac du lieu.
 *
 * Dung JdbcTemplate.batchUpdate thay vi JpaRepository.save() tung dong mot:
 * voi GenerationType.IDENTITY, Hibernate khong the gom batch insert, nen
 * save() 500,000 lan se rat cham. Insert tho qua JDBC nhanh hon nhieu.
 */
@Component
public class LeaderboardSeeder implements CommandLineRunner {

	private static final int POSTGRES_BATCH_SIZE = 1000;
	private static final int REDIS_BATCH_SIZE = 5000;

	private final JdbcTemplate jdbcTemplate;
	private final PlayerScoreRepository playerScoreRepository;
	private final RedisLeaderboardService redisLeaderboardService;

	@Value("${app.leaderboard.seed-count:500000}")
	private int seedCount;

	public LeaderboardSeeder(JdbcTemplate jdbcTemplate, PlayerScoreRepository playerScoreRepository,
			RedisLeaderboardService redisLeaderboardService) {
		this.jdbcTemplate = jdbcTemplate;
		this.playerScoreRepository = playerScoreRepository;
		this.redisLeaderboardService = redisLeaderboardService;
	}

	private record PlayerSeed(String name, double score) {
	}

	@Override
	public void run(String... args) {
		if (playerScoreRepository.count() > 0) {
			System.out.println("Da co du lieu trong player_score, bo qua seeding.");
			return;
		}

		System.out.println("Dang sinh " + seedCount + " nguoi choi ngau nhien...");
		List<PlayerSeed> seeds = generateSeeds(seedCount);

		long startPostgres = System.currentTimeMillis();
		seedPostgres(seeds);
		System.out.println("Seed Postgres xong sau " + (System.currentTimeMillis() - startPostgres) + "ms");

		long startRedis = System.currentTimeMillis();
		seedRedis(seeds);
		System.out.println("Seed Redis ZSet xong sau " + (System.currentTimeMillis() - startRedis) + "ms");
	}

	private List<PlayerSeed> generateSeeds(int count) {
		List<PlayerSeed> seeds = new ArrayList<>(count);
		for (int i = 1; i <= count; i++) {
			double score = ThreadLocalRandom.current().nextDouble(0, 100_000);
			seeds.add(new PlayerSeed("Player-" + i, score));
		}
		return seeds;
	}

	private void seedPostgres(List<PlayerSeed> seeds) {
		String sql = "INSERT INTO player_score (player_name, score) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(sql, seeds, POSTGRES_BATCH_SIZE, (ps, seed) -> {
			ps.setString(1, seed.name());
			ps.setDouble(2, seed.score());
		});
	}

	private void seedRedis(List<PlayerSeed> seeds) {
		for (int start = 0; start < seeds.size(); start += REDIS_BATCH_SIZE) {
			List<PlayerSeed> chunk = seeds.subList(start, Math.min(start + REDIS_BATCH_SIZE, seeds.size()));
			Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>(chunk.size());
			for (PlayerSeed seed : chunk) {
				tuples.add(ZSetOperations.TypedTuple.of(seed.name(), seed.score()));
			}
			redisLeaderboardService.addAll(tuples);
		}
	}
}
