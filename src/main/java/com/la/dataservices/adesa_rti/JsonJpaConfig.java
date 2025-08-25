package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("legacy-json")
@EnableJpaRepositories(basePackageClasses = VehicleRepositoryJson.class)
@EntityScan(basePackageClasses = VehicleEntityJson.class)
class JsonJpaConfig { }
