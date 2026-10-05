package com.example.smartkost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartkostApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartkostApplication.class, args);
	}

}
