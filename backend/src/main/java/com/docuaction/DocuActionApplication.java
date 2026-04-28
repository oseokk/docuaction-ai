package com.docuaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DocuActionApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocuActionApplication.class, args);
	}

}
