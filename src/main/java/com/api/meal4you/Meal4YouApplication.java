package com.api.meal4you;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Meal4YouApplication {

	public static void main(String[] args) {
		SpringApplication.run(Meal4YouApplication.class, args);
	}
}
