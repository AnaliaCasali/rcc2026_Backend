package edu.ies63.rcc2026_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Rcc2026BackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(Rcc2026BackendApplication.class, args);
	}

}
