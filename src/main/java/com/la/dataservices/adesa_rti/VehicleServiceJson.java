package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleServiceJson implements VehicleService {

    private final VehicleRepositoryJson repo;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public VehicleEntityJson upsertFromCloudEvent(CloudEvent evt) {
        if (evt == null || evt.getData() == null) {
            throw new IllegalArgumentException("CloudEvent data is required");
        }
        JsonNode d = evt.getData();

        String vin   = text(d, "vin");
        String extId = text(d, "vehicleId");

        VehicleEntityJson entity = findExisting(vin, extId).orElseGet(VehicleEntityJson::new);

        // Scalars
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

        // JSON strings
        entity.setAdditionalVehicleImageUrlsJson(jsonOrNull(d.get("additionalVehicleImageUrls")));
        entity.setSellerAnnouncementsJson(jsonOrNull(d.get("sellerAnnouncements")));
        entity.setVehiclePartsJson(jsonOrNull(d.get("vehicleParts")));
        entity.setRunlistDealerCodesJson(jsonOrNull(d.get("runlistDealerCodes")));
        entity.setLiveblockDealerCodesJson(jsonOrNull(d.get("liveblockDealerCodes")));
        entity.setDealerblockDealerCodesJson(jsonOrNull(d.get("dealerblockDealerCodes")));
        entity.setDamagesJson(jsonOrNull(d.get("damages")));
        entity.setTiresJson(jsonOrNull(d.get("tires")));

        return repo.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<VehicleEntityJson> findExisting(String vin, String externalId) {
        if (vin != null && !vin.isBlank()) {
            Optional<VehicleEntityJson> byVin = repo.findByVin(vin);
            if (byVin.isPresent()) return byVin;
        }
        if (externalId != null && !externalId.isBlank()) {
            Optional<VehicleEntityJson> byId = repo.findByExternalVehicleId(externalId);
            if (byId.isPresent()) return byId;
        }
        return Optional.empty();
    }

    private static String text(JsonNode n, String f) {
        return n != null && n.has(f) && !n.get(f).isNull() ? n.get(f).asText() : null;
    }
    private static Integer intOrNull(JsonNode n, String f) {
        return n != null && n.has(f) && !n.get(f).isNull() ? n.get(f).asInt() : null;
    }
    private static Double doubleOrNull(JsonNode n, String f) {
        return n != null && n.has(f) && !n.get(f).isNull() ? n.get(f).asDouble() : null;
    }

    private String jsonOrNull(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try { return objectMapper.writeValueAsString(node); }
        catch (Exception e) { return null; }
    }
}
