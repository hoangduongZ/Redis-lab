package com.redislab.lab01.book;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Buoc 3 cua giao an: bang trang khong lo dung chung cho tat ca instance.
 * Spring Boot tu lo het viec kiem tra/luu, chi can gan @Cacheable.
 */
@Service
public class RedisCacheBookService {

	private final BookFinder bookFinder;
	private final StringRedisTemplate redisTemplate;

	public RedisCacheBookService(BookFinder bookFinder, StringRedisTemplate redisTemplate) {
		this.bookFinder = bookFinder;
		this.redisTemplate = redisTemplate;
	}

	@Cacheable(value = "books", key = "#title")
	public String getBook(String title) throws InterruptedException {
		return bookFinder.fetchFromDatabase(title);
	}

	/**
	 * Chinh xac nhung gi @Cacheable lam ho ban o phia tren, viet tay bang
	 * StringRedisTemplate de thay "phep thuat" thuc chat la gi. Dung chung
	 * mot cai ten key ("books::title") de so sanh cong bang trong Redis Commander.
	 */
	public String getBookManual(String title) throws InterruptedException {
		String redisKey = "books::" + title;

		String cached = redisTemplate.opsForValue().get(redisKey);
		if (cached != null) {
			System.out.println("Co san trong Redis, tra ve ngay!");
			return cached;
		}

		String data = bookFinder.fetchFromDatabase(title);
		redisTemplate.opsForValue().set(redisKey, data, Duration.ofMinutes(5));
		return data;
	}
}
