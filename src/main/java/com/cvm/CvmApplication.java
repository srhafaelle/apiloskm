package com.cvm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CvmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvmApplication.class, args);
	}

}
