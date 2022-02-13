package com.bagongkia.stev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class StevReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(StevReportApplication.class, args);
	}

}