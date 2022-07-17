package com.bagongkia.stev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@PropertySources(value = {
		@PropertySource(value = "file:report.properties"),
		@PropertySource(value = "classpath:application.properties"),
})
public class StevReportApplication {

	public static void main(String[] args) {
		SpringApplication.run(StevReportApplication.class, args);
	}

}