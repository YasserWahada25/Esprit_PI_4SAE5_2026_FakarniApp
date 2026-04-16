package com.alzheimer.suivi_engagement_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.alzheimer.suivi_engagement_service.client")
public class SuiviEngagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SuiviEngagementServiceApplication.class, args);
	}

}
