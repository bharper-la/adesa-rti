package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

//@Profile("!legacy-json")
public interface VehicleRepositoryJpa extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByVin(String vin);
    Optional<VehicleEntity> findByExternalVehicleId(String externalVehicleId);
}
