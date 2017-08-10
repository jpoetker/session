package com.github.jpoetker.spike.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisSessionOverrideApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisSessionOverrideApplication.class, args);
	}
}
