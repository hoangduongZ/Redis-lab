package com.redislab.lab01.book;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class BookController {

	private final NoCacheBookService noCacheBookService;
	private final ManualCacheBookService manualCacheBookService;
	private final RedisCacheBookService redisCacheBookService;

	public BookController(NoCacheBookService noCacheBookService,
			ManualCacheBookService manualCacheBookService,
			RedisCacheBookService redisCacheBookService) {
		this.noCacheBookService = noCacheBookService;
		this.manualCacheBookService = manualCacheBookService;
		this.redisCacheBookService = redisCacheBookService;
	}

	@GetMapping("/api/no-cache/books/{title}")
	public Map<String, Object> noCache(@PathVariable String title) throws InterruptedException {
		return timed(() -> noCacheBookService.getBook(title));
	}

	@GetMapping("/api/manual-cache/books/{title}")
	public Map<String, Object> manualCache(@PathVariable String title) throws InterruptedException {
		return timed(() -> manualCacheBookService.getBook(title));
	}

	@GetMapping("/api/redis-cache/books/{title}")
	public Map<String, Object> redisCache(@PathVariable String title) throws InterruptedException {
		return timed(() -> redisCacheBookService.getBook(title));
	}

	@GetMapping("/api/redis-cache-manual/books/{title}")
	public Map<String, Object> redisCacheManual(@PathVariable String title) throws InterruptedException {
		return timed(() -> redisCacheBookService.getBookManual(title));
	}

	private interface BookLookup {
		String get() throws InterruptedException;
	}

	private Map<String, Object> timed(BookLookup lookup) throws InterruptedException {
		long start = System.currentTimeMillis();
		String content = lookup.get();
		long elapsedMs = System.currentTimeMillis() - start;
		return Map.of("content", content, "elapsedMs", elapsedMs);
	}
}
