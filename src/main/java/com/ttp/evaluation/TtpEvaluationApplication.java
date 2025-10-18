package com.ttp.evaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Единая Аналитическая Информационная Система (ЕАИС)
 * для оценки эффективности мер ТТП
 *
 * Модульный монолит на Spring Boot 3.2 и Java 21
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class TtpEvaluationApplication {

	public static void main(String[] args) {
		SpringApplication.run(TtpEvaluationApplication.class, args);
	}
}