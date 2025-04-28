package com.biomedica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BioMedicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BioMedicaApplication.class, args);
	}

}
