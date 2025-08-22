package com.la.dataservices.adesa_rti;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByVin(String vin);
    Optional<VehicleEntity> findByExternalVehicleId(String externalVehicleId);
}
