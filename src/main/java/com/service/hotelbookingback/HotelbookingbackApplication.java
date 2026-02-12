package com.service.hotelbookingback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
public class HotelbookingbackApplication extends RuntimeException {

	public static void main(String[] args) {
		SpringApplication.run(HotelbookingbackApplication.class, args);
	}

}
