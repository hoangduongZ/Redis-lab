package com.redislab.lab01.book;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Buoc 2 cua giao an: chiec bang trang tu che, la mot HashMap nam trong RAM
 * cua CHINH instance nay. Neu chay 2 instance, moi instance co bang rieng.
 */
@Service
public class ManualCacheBookService {

	private final BookFinder bookFinder;
	private final Map<String, String> bangTrang = new ConcurrentHashMap<>();

	public ManualCacheBookService(BookFinder bookFinder) {
		this.bookFinder = bookFinder;
	}

	public String getBook(String title) throws InterruptedException {
		if (bangTrang.containsKey(title)) {
			System.out.println("Co san tren bang trang, tra ve ngay!");
			return bangTrang.get(title);
		}

		String data = bookFinder.fetchFromDatabase(title);
		bangTrang.put(title, data);
		return data;
	}
}
