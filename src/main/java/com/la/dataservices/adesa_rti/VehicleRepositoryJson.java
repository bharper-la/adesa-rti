package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Profile("legacy-json")
public interface VehicleRepositoryJson extends JpaRepository<VehicleEntityJson, Long> {
    Optional<VehicleEntityJson> findByVin(String vin);
    Optional<VehicleEntityJson> findByExternalVehicleId(String externalVehicleId);
}
