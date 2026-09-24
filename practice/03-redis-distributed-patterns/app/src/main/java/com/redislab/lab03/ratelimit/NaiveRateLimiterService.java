package com.redislab.lab03.ratelimit;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * "Noi dau" tuong tu ticket: dem rieng tung instance bang AtomicInteger trong
 * RAM. Chay 2 instance = 2 bo dem rieng cho cung 1 IP, nen tong so request
 * lot qua co the gap doi nguong mong muon.
 */
@Service
public class NaiveRateLimiterService {

	private static final int LIMIT_PER_SECOND = 5;

	private final ConcurrentHashMap<String, AtomicInteger> countersBySecond = new ConcurrentHashMap<>();

	public boolean allow(String ip) {
		long currentSecond = System.currentTimeMillis() / 1000;
		String bucketKey = ip + ":" + currentSecond;
		int count = countersBySecond.computeIfAbsent(bucketKey, k -> new AtomicInteger(0)).incrementAndGet();
		return count <= LIMIT_PER_SECOND;
	}
}
