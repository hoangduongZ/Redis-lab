package com.redislab.lab01.book;

import org.springframework.stereotype.Component;

@Component
public class BookFinder {

	private final BookRepository bookRepository;

	public BookFinder(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	/**
	 * Day chinh la "cai ham" (Postgres). Them delay gia lap do tre o cung
	 * de con cam nhan duoc "noi dau" ma giao an mo ta, thay vi Postgres local
	 * tra loi nhanh qua khong thay duoc su khac biet.
	 */
	public String fetchFromDatabase(String title) throws InterruptedException {
		System.out.println("Dang chui xuong ham tim cuon '" + title + "'...");
		Thread.sleep(2000);
		return bookRepository.findByTitle(title)
				.map(Book::getContent)
				.orElseThrow(() -> new IllegalArgumentException("Khong tim thay sach: " + title));
	}
}
