package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("relational")
@EnableJpaRepositories(basePackageClasses = VehicleRepositoryJpa.class)
@EntityScan(basePackageClasses = VehicleEntity.class)
class RelationalJpaConfig { }
