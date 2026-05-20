package com.ajcarpinello.Pern_Merch_Website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PernMerchWebsiteApplication {
	public static void main(String[] args) {
		SpringApplication.run(PernMerchWebsiteApplication.class, args);
	}
}
