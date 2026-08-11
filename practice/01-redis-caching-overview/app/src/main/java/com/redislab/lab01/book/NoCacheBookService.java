package com.redislab.lab01.book;

import org.springframework.stereotype.Service;

/**
 * Buoc 1 cua giao an: khong co bang trang nao ca.
 * Moi lan goi la mot lan chui xuong ham, du la cung mot cau hoi.
 */
@Service
public class NoCacheBookService {

	private final BookFinder bookFinder;

	public NoCacheBookService(BookFinder bookFinder) {
		this.bookFinder = bookFinder;
	}

	public String getBook(String title) throws InterruptedException {
		return bookFinder.fetchFromDatabase(title);
	}
}
