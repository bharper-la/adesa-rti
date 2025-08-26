package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile("relational")
@Component
public interface VehicleRepositoryJpa extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByVin(String vin);
    Optional<VehicleEntity> findByExternalVehicleId(String externalVehicleId);
}
