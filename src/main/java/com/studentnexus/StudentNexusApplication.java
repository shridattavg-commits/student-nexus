package com.studentnexus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StudentNexusApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentNexusApplication.class, args);
	}

	@Bean
	public CommandLineRunner logMongoUri(@Value("${spring.data.mongodb.uri}") String mongoUri) {
		return args -> System.out.println(">>> MONGO URI IN USE: " + mongoUri);
	}
}