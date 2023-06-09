package com.filipegeniselli.backendtechassignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendTechAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendTechAssignmentApplication.class, args);
	}

}
