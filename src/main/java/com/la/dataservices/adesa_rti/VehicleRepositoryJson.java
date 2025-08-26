package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface VehicleRepositoryJson extends JpaRepository<VehicleEntityJson, Long> {
    Optional<VehicleEntityJson> findByVin(String vin);
    Optional<VehicleEntityJson> findByExternalVehicleId(String externalVehicleId);
}
