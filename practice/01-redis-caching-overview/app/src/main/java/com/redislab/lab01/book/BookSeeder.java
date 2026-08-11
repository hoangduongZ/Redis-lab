package com.redislab.lab01.book;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BookSeeder implements CommandLineRunner {

	private final BookRepository bookRepository;

	public BookSeeder(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Override
	public void run(String... args) {
		if (bookRepository.count() > 0) {
			return;
		}

		bookRepository.save(new Book("Harry Potter", "Cau chuyen ve cau be phu thuy Harry Potter"));
		bookRepository.save(new Book("Doraemon", "Chu meo may den tu tuong lai"));
		bookRepository.save(new Book("Sherlock Holmes", "Vi tham tu lung danh cua nuoc Anh"));
	}
}
