package com.redislab.lab01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Lab01Application {

	public static void main(String[] args) {
		SpringApplication.run(Lab01Application.class, args);
	}

}
