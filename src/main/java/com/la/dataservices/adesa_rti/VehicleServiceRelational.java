package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Profile("!legacy-json")
@RequiredArgsConstructor
public class VehicleServiceRelational implements VehicleService {

    private final VehicleRepositoryJpa vehicleRepository;

    @Transactional
    @Override
    public VehicleEntity upsertFromCloudEvent(CloudEvent evt) {
        if (evt == null || evt.getData() == null) {
            throw new IllegalArgumentException("CloudEvent data is required");
        }
        JsonNode d = evt.getData();

        String vin = text(d, "vin");
        String extId = text(d, "vehicleId");

        VehicleEntity entity = findExisting(vin, extId).orElseGet(VehicleEntity::new);

        entity.setExternalVehicleId(extId);
        entity.setVin(vin);
        entity.setYear(intOrNull(d, "year"));
        entity.setMakeName(text(d, "makeName"));
        entity.setModelName(text(d, "modelName"));
        entity.setSeriesName(text(d, "seriesName"));
        entity.setBodyStyleName(text(d, "bodyStyleName"));
        entity.setEngineName(text(d, "engineName"));
        entity.setEngineDisplacement(doubleOrNull(d, "engineDisplacement"));
        entity.setCylinders(intOrNull(d, "cylinders"));
        entity.setTransmission(text(d, "transmission"));
        entity.setDrivetrain(text(d, "drivetrain"));
        entity.setFuelType(text(d, "fuelType"));
        entity.setInteriorColor(text(d, "interiorColor"));
        entity.setExteriorColor(text(d, "exteriorColor"));
        entity.setVehicleType(text(d, "vehicleType"));
        entity.setMileage(intOrNull(d, "mileage"));
        entity.setUnitOfMeasure(text(d, "unitOfMeasure"));
        entity.setOdometerCondition(text(d, "odometerCondition"));
        entity.setState(text(d, "state"));
        entity.setPostalCode(text(d, "postalCode"));
        entity.setCurrentHighBid(intOrNull(d, "currentHighBid"));
        entity.setBuyNowPrice(intOrNull(d, "buyNowPrice"));
        entity.setAuctionEndDate(text(d, "auctionEndDate"));
        entity.setSellerName(text(d, "sellerName"));

        // map damages (relational)
        entity.getDamages().clear();
        JsonNode damages = d.get("damages");
        if (damages != null && damages.get("items") != null && damages.get("items").isArray()) {
            List<DamageItemEntity> items = new ArrayList<>();
            for (JsonNode it : damages.get("items")) {
                DamageItemEntity de = DamageItemEntity.builder()
                        .vehicle(entity)
                        .description(text(it, "item"))
                        .photoUrl(text(it, "photoUrl"))
                        .build();
                items.add(de);
            }
            entity.getDamages().addAll(items);
        }

        return vehicleRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<VehicleEntity> findExisting(String vin, String externalId) {
        if (vin != null && !vin.isBlank()) {
            Optional<VehicleEntity> byVin = vehicleRepository.findByVin(vin);
            if (byVin.isPresent()) return byVin;
        }
        if (externalId != null && !externalId.isBlank()) {
            Optional<VehicleEntity> byId = vehicleRepository.findByExternalVehicleId(externalId);
            if (byId.isPresent()) return byId;
        }
        return Optional.empty();
    }

    private static String text(JsonNode n, String field) {
        return n != null && n.has(field) && !n.get(field).isNull() ? n.get(field).asText() : null;
    }
    private static Integer intOrNull(JsonNode n, String field) {
        return n != null && n.has(field) && !n.get(field).isNull() ? n.get(field).asInt() : null;
    }
    private static Double doubleOrNull(JsonNode n, String field) {
        return n != null && n.has(field) && !n.get(field).isNull() ? n.get(field).asDouble() : null;
    }
}
