package com.la.dataservices.adesa_rti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication()
public class AdesaRtiApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 👇 prevent ErrorPageFilter registration in external Tomcat
		setRegisterErrorPageFilter(false);
		return builder.sources(AdesaRtiApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AdesaRtiApplication.class, args);
	}
}

