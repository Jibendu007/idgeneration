package com.volante.idgeneration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IdgenerationApplication {
	public static void main(String[] args) {
		SpringApplication.run(IdgenerationApplication.class, args);
	}
}