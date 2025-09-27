package com.api.meal4you;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class Meal4YouApplication {

	public static void main(String[] args) {
		SpringApplication.run(Meal4YouApplication.class, args);
	}

}
