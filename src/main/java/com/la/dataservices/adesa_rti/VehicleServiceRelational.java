package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleEntity upsertFromCloudEvent(CloudEvent evt) {
        if (evt == null || evt.getData() == null) {
            throw new IllegalArgumentException("CloudEvent data is required");
        }
        JsonNode d = evt.getData();

        // Prefer VIN; fallback to external vehicle ID
        String vin   = text(d, "vin");
        String extId = text(d, "vehicleId");

        VehicleEntity entity = findExisting(vin, extId).orElseGet(VehicleEntity::new);

        // Identity
        entity.setExternalVehicleId(extId);
        entity.setVin(vin);

        // Core specs
        entity.setYear(intOrNull(d, "year"));
        entity.setMakeName(text(d, "makeName"));
        entity.setModelName(text(d, "modelName"));
        entity.setSeriesName(text(d, "seriesName"));
        entity.setTrim(text(d, "trim"));
        entity.setBodyStyleName(text(d, "bodyStyleName"));
        entity.setEngineName(text(d, "engineName"));
        entity.setEngineDisplacement(doubleOrNull(d, "engineDisplacement"));
        entity.setCylinders(intOrNull(d, "cylinders"));
        entity.setTransmission(text(d, "transmission"));
        entity.setDrivetrain(textEither(d, "drivetrain", "driveTrain")); // handle both shapes
        entity.setFuelType(text(d, "fuelType"));
        entity.setInteriorColor(text(d, "interiorColor"));
        entity.setExteriorColor(text(d, "exteriorColor"));
        entity.setVehicleType(text(d, "vehicleType"));

        // Odo/location/pricing/dates
        entity.setMileage(intOrNull(d, "mileage"));
        entity.setUnitOfMeasure(text(d, "unitOfMeasure"));
        entity.setOdometerCondition(text(d, "odometerCondition"));
        entity.setState(text(d, "state"));
        entity.setPostalCode(text(d, "postalCode"));
        entity.setCurrentHighBid(intOrNull(d, "currentHighBid"));
        entity.setBuyNowPrice(intOrNull(d, "buyNowPrice"));
        entity.setAuctionEndDate(text(d, "auctionEndDate"));
        entity.setSaleEventDate(text(d, "saleEventDate"));
        entity.setSellerName(text(d, "sellerName"));

        // Media & links
        entity.setPrimaryImageUrl(text(d, "primaryImageUrl"));
        entity.setVehicleDetailUrl(text(d, "vehicleDetailUrl"));
        entity.setImageViewerUrl(text(d, "imageViewerUrl"));

        // Collections (replace contents)
        replaceList(entity.getSaleChannels(), listText(d, "saleChannels"));
        replaceList(entity.getAdditionalVehicleImageUrls(), listText(d, "additionalVehicleImageUrls"));
        replaceList(entity.getSellerAnnouncements(), listText(d, "sellerAnnouncements"));
        replaceList(entity.getVehicleParts(), listText(d, "vehicleParts"));
        replaceList(entity.getTires(), listText(d, "tires"));

        // Auction/vendor/listing meta
        entity.setHasPriorPaintwork(boolOrNull(d, "hasPriorPaintwork"));
        entity.setSellerType(text(d, "sellerType"));
        entity.setLotNumber(text(d, "lotNumber"));
        entity.setRunNumber(text(d, "runNumber"));
        entity.setLane(text(d, "lane"));
        entity.setAuctionVendor(text(d, "auctionVendor"));
        entity.setAssetType(text(d, "assetType"));
        entity.setSaleEventType(text(d, "saleEventType"));
        entity.setOpenEqualsReserve(boolOrNull(d, "openEqualsReserve"));
        entity.setRlLbListingCategory(textEither(d, "rlLbListingCategory", "rlLBListingCategory", "dealerblockListingCategory"));

        // Condition / grading
        entity.setVehicleGrade(doubleOrNull(d, "vehicleGrade"));
        entity.setIsAutoGrade(boolOrNull(d, "isAutoGrade"));
        entity.setTitleState(text(d, "titleState"));
        entity.setFrameDamage(intOrNull(d, "frameDamage"));

        // Inspection
        entity.setInspectionDate(text(d, "inspectionDate"));
        entity.setInspectionCompany(text(d, "inspectionCompany"));
        entity.setInspectionComments(text(d, "inspectionComments"));

        // Structure / drive / keys, etc.
        entity.setStructuralDamage(text(d, "structuralDamage"));
        entity.setDrivable(textEither(d, "drivable", "driveable")); // keep raw value ("Yes"/"No") as provided
        entity.setOdors(text(d, "odors"));
        entity.setKeys(text(d, "keys"));
        entity.setRemotes(text(d, "remotes"));
        entity.setManuals(text(d, "manuals"));

        // Status flags
        entity.setInTransitIndicator(boolOrNull(d, "inTransitIndicator"));
        entity.setAdesaAssurance(boolOrNull(d, "adesaAssurance"));
        entity.setAutoCheck(text(d, "autoCheck"));
        entity.setFirstRun(boolOrNull(d, "firstRun"));
        entity.setLastRun(boolOrNull(d, "lastRun"));
        entity.setPdi(boolOrNull(d, "pdi"));
        entity.setPsi(boolOrNull(d, "psi"));
        entity.setPriceReduced(boolOrNull(d, "priceReduced"));
        entity.setThirdPartyCRExists(boolOrNull(d, "thirdPartyCRExists"));

        // Misc IDs & times
        entity.setCountry(intOrNull(d, "country"));
        entity.setSaleEventId(longOrNull(d, "saleEventId"));
        entity.setAutobidStartTime(text(d, "autobidStartTime"));
        entity.setPsiEligibilityAutobid(boolOrNull(d, "psiEligibilityAutobid"));

        entity.setPriceUpdated(text(d, "priceUpdated"));
        entity.setStockNumber(text(d, "stockNumber"));
        entity.setHighValueOptions(text(d, "highValueOptions"));
        entity.setDoors(intOrNull(d, "doors"));
        entity.setStatements(text(d, "statements"));
        entity.setSmokedIn(text(d, "smokedIn"));
        entity.setTestDriven(text(d, "testDriven"));
        entity.setAmsSiteId(intOrNull(d, "amsSiteId"));

        // Damages summary + items
        JsonNode damages = d.get("damages");
        entity.setDamagesCount(intOrNull(damages, "damagesCount"));
        entity.setDamagesAmount(intOrNull(damages, "damagesAmount"));

        entity.getDamages().clear();
        if (damages != null && damages.get("items") != null && damages.get("items").isArray()) {
            for (JsonNode it : damages.get("items")) {
                DamageItemEntity de = DamageItemEntity.builder()
                        .vehicle(entity)
                        .description(text(it, "item"))
                        .photoUrl(text(it, "photoUrl"))
                        .build();
                entity.getDamages().add(de);
            }
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

    // ---------- helpers ----------

    private static void replaceList(List<String> target, List<String> incoming) {
        target.clear();
        if (incoming != null && !incoming.isEmpty()) target.addAll(incoming);
    }

    private static List<String> listText(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull() || !n.get(field).isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode v : n.get(field)) {
            if (v != null && !v.isNull()) out.add(v.asText());
        }
        return out;
    }

    private static String text(JsonNode n, String field) {
        return (n != null && n.has(field) && !n.get(field).isNull()) ? n.get(field).asText() : null;
    }

    private static String textEither(JsonNode n, String... fields) {
        if (n == null) return null;
        for (String f : fields) {
            if (n.has(f) && !n.get(f).isNull()) return n.get(f).asText();
        }
        return null;
    }

    private static Integer intOrNull(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) return null;
        JsonNode v = n.get(field);
        if (v.isInt() || v.isLong()) return v.asInt();
        if (v.isNumber()) return (int) v.asDouble();
        try { return Integer.valueOf(v.asText().replaceAll("[^0-9\\-]", "")); }
        catch (Exception e) { return null; }
    }

    private static Long longOrNull(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) return null;
        JsonNode v = n.get(field);
        if (v.isLong() || v.isInt()) return v.asLong();
        if (v.isNumber()) return (long) v.asDouble();
        try { return Long.valueOf(v.asText().replaceAll("[^0-9\\-]", "")); }
        catch (Exception e) { return null; }
    }

    private static Double doubleOrNull(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) return null;
        JsonNode v = n.get(field);
        if (v.isNumber()) return v.asDouble();
        try { return Double.valueOf(v.asText().replaceAll("[^0-9eE\\+\\-\\.]", "")); }
        catch (Exception e) { return null; }
    }

    private static Boolean boolOrNull(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) return null;
        JsonNode v = n.get(field);
        if (v.isBoolean()) return v.asBoolean();
        if (v.isNumber()) return v.asInt() != 0;
        String s = v.asText();
        if (s == null) return null;
        s = s.trim().toLowerCase();
        if (s.isEmpty()) return null;
        if (s.equals("true") || s.equals("yes") || s.equals("y") || s.equals("1")) return true;
        if (s.equals("false") || s.equals("no") || s.equals("n") || s.equals("0")) return false;
        return null;
    }
}
