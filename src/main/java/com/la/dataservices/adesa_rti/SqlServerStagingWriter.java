package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Objects;

@Slf4j
@Component
@Profile("prod") // <-- only active when you run with -Dspring.profiles.active=prod
@RequiredArgsConstructor
public class SqlServerStagingWriter {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    // Keep the column list in sync with stag.adesa_json. If you add or remove columns in SQL,
    // adjust both the list below and the parameter bindings in the same order.
    private static final String INSERT_SQL = """
        INSERT INTO stag.adesa_json (
            eventType, eventDate, vehicleId, vin, sellerName, [year],
            makeName, modelName, seriesName, bodyStyleName, engineName, engineDisplacement, cylinders,
            transmission, drivetrain, fuelType, interiorColor, exteriorColor, vehicleType, mileage,
            unitOfMeasure, odometerCondition, [state], postalCode, currentHighBid, buyNowPrice,
            auctionEndDate, primaryImageUrl, vehicleDetailUrl, imageViewerUrl,
            additionalVehicleImageUrls, sellerAnnouncements, vehicleParts,
            hasPriorPaintwork, sellerType, lotNumber, runNumber, lane, auctionVendor,
            runlistDealerCodes, liveblockDealerCodes, dealerblockDealerCodes, assetType,
            saleEventType, saleEventDate, damagesCount, damagesAmount, damagesItems,
            openEqualsReserve, vehicleGrade, isAutoGrade, titleState, frameDamage, tires
        )
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;

    /**
     * Insert one RTI payload row into stag.adesa_json.
     * @param ev CloudEvent wrapper (we take most fields from ev.getData()).
     */
    public void insertFromCloudEvent(CloudEvent ev) {
        JsonNode d = Objects.requireNonNull(ev.getData(), "CloudEvent.data is required");

        // Required by the DDL
        String eventType = text(d, "eventType");                     // nvarchar(32) not null
        Timestamp eventDate = ts(d, "eventDate");                    // datetime     not null
        Long vehicleId = longOrNull(d, "vehicleId");                 // bigint       not null (must be numeric)
        String vin = text(d, "vin");                                 // nvarchar(17) not null
        if (eventType == null || eventDate == null || vehicleId == null || vin == null) {
            throw new IllegalStateException("""
                    stag.adesa_json requires non-null (eventType, eventDate, vehicleId(bigint), vin).
                    Got: eventType=%s, eventDate=%s, vehicleId=%s, vin=%s
                    """.formatted(eventType, eventDate, vehicleId, vin));
        }

        // Optionals / strings
        Integer year                 = intOrNull(d, "year");
        String sellerName            = text(d, "sellerName");
        String makeName              = text(d, "makeName");
        String modelName             = text(d, "modelName");
        String seriesName            = text(d, "seriesName");
        String bodyStyleName         = text(d, "bodyStyleName");
        String engineName            = text(d, "engineName");
        Double engineDisplacement    = doubleOrNull(d, "engineDisplacement");
        Integer cylinders            = intOrNull(d, "cylinders");
        String transmission          = text(d, "transmission");
        String drivetrain            = text(d, "driveTrain"); // NOTE: v2 sample uses driveTrain (camel T). DDL uses drivetrain col name.
        String fuelType              = text(d, "fuelType");
        String interiorColor         = text(d, "interiorColor");
        String exteriorColor         = text(d, "exteriorColor");
        String vehicleType           = text(d, "vehicleType");
        Integer mileage              = intOrNull(d, "mileage");
        String unitOfMeasure         = text(d, "unitOfMeasure");
        String odometerCondition     = text(d, "odometerCondition");
        String state                 = text(d, "state");
        String postalCode            = text(d, "postalCode");
        Integer currentHighBid       = intOrNull(d, "currentHighBid");
        Integer buyNowPrice          = intOrNull(d, "buyNowPrice");
        Timestamp auctionEndDate     = ts(d, "auctionEndDate"); // parse if ISO-8601; null ok
        String primaryImageUrl       = text(d, "primaryImageUrl");
        String vehicleDetailUrl      = text(d, "vehicleDetailUrl");
        String imageViewerUrl        = text(d, "imageViewerUrl");

        // Arrays -> JSON strings
        String jsonAdditionalImages  = json(d.get("additionalVehicleImageUrls"));
        String jsonSellerAnnouncements = json(d.get("sellerAnnouncements"));
        String jsonVehicleParts      = json(d.get("vehicleParts"));
        String jsonRunlistCodes      = json(d.get("runlistDealerCodes"));
        String jsonLiveblockCodes    = json(d.get("liveblockDealerCodes"));
        String jsonDealerblockCodes  = json(d.get("dealerblockDealerCodes"));

        // Other optionals
        Boolean hasPriorPaintwork    = boolOrNull(d, "hasPriorPaintwork");
        String sellerType            = text(d, "sellerType");
        String lotNumber             = text(d, "lotNumber");
        String runNumber             = text(d, "runNumber");
        String lane                  = text(d, "lane");
        String auctionVendor         = text(d, "auctionVendor");
        String assetType             = text(d, "assetType");
        String saleEventType         = text(d, "saleEventType");
        Timestamp saleEventDate      = ts(d, "saleEventDate");

        // damages block
        JsonNode damages             = d.get("damages");
        Integer damagesCount         = (damages != null && damages.hasNonNull("damagesCount"))
                ? safeInt(damages.get("damagesCount")) : null;
        Integer damagesAmount        = (damages != null && damages.hasNonNull("damagesAmount"))
                ? safeInt(damages.get("damagesAmount")) : null;
        String jsonDamagesItems      = (damages != null) ? json(damages.get("items")) : null;

        Boolean openEqualsReserve    = boolOrNull(d, "openEqualsReserve");
        Double vehicleGrade          = doubleOrNull(d, "vehicleGrade");
        Boolean isAutoGrade          = boolOrNull(d, "isAutoGrade");
        String titleState            = text(d, "titleState");
        Integer frameDamage          = intOrNull(d, "frameDamage"); // looks like 0/1 in samples
        String jsonTires             = json(d.get("tires"));

        // Execute
        jdbc.update(INSERT_SQL,
                ps -> {
                    int i = 1;
                    ps.setString(i++, eventType);
                    ps.setTimestamp(i++, eventDate);
                    ps.setLong(i++, vehicleId);
                    ps.setString(i++, vin);
                    ps.setString(i++, sellerName);
                    setInt(ps, i++, year);

                    ps.setString(i++, makeName);
                    ps.setString(i++, modelName);
                    ps.setString(i++, seriesName);
                    ps.setString(i++, bodyStyleName);
                    ps.setString(i++, engineName);
                    setDouble(ps, i++, engineDisplacement);
                    setInt(ps, i++, cylinders);

                    ps.setString(i++, transmission);
                    ps.setString(i++, drivetrain);
                    ps.setString(i++, fuelType);
                    ps.setString(i++, interiorColor);
                    ps.setString(i++, exteriorColor);
                    ps.setString(i++, vehicleType);
                    setInt(ps, i++, mileage);

                    ps.setString(i++, unitOfMeasure);
                    ps.setString(i++, odometerCondition);
                    ps.setString(i++, state);
                    ps.setString(i++, postalCode);
                    setInt(ps, i++, currentHighBid);
                    setInt(ps, i++, buyNowPrice);

                    ps.setTimestamp(i++, auctionEndDate);
                    ps.setString(i++, primaryImageUrl);
                    ps.setString(i++, vehicleDetailUrl);
                    ps.setString(i++, imageViewerUrl);

                    ps.setString(i++, jsonAdditionalImages);
                    ps.setString(i++, jsonSellerAnnouncements);
                    ps.setString(i++, jsonVehicleParts);

                    setBoolean(ps, i++, hasPriorPaintwork);
                    ps.setString(i++, sellerType);
                    ps.setString(i++, lotNumber);
                    ps.setString(i++, runNumber);
                    ps.setString(i++, lane);
                    ps.setString(i++, auctionVendor);

                    ps.setString(i++, jsonRunlistCodes);
                    ps.setString(i++, jsonLiveblockCodes);
                    ps.setString(i++, jsonDealerblockCodes);
                    ps.setString(i++, assetType);

                    ps.setString(i++, saleEventType);
                    ps.setTimestamp(i++, saleEventDate);
                    setInt(ps, i++, damagesCount);
                    setInt(ps, i++, damagesAmount);
                    ps.setString(i++, jsonDamagesItems);

                    setBoolean(ps, i++, openEqualsReserve);
                    setDouble(ps, i++, vehicleGrade);
                    setBoolean(ps, i++, isAutoGrade);
                    ps.setString(i++, titleState);
                    setInt(ps, i++, frameDamage);
                    ps.setString(i++, jsonTires);
                });

        log.debug("Inserted VIN={} vehicleId={} into stag.adesa_json", vin, vehicleId);
    }

    // --- helpers -------------------------------------------------------------

    private String text(JsonNode n, String field) {
        return (n != null && n.has(field) && !n.get(field).isNull()) ? n.get(field).asText() : null;
    }

    private Integer intOrNull(JsonNode n, String field) {
        return (n != null && n.has(field) && !n.get(field).isNull()) ? n.get(field).asInt() : null;
    }

    private int safeInt(JsonNode node) {
        try {
            return node.isInt() ? node.asInt() : Integer.parseInt(node.asText());
        } catch (Exception e) {
            return 0;
        }
    }

    private Double doubleOrNull(JsonNode n, String field) {
        return (n != null && n.has(field) && !n.get(field).isNull()) ? n.get(field).asDouble() : null;
    }

    private Boolean boolOrNull(JsonNode n, String field) {
        if (n == null || !n.has(field) || n.get(field).isNull()) return null;
        JsonNode v = n.get(field);
        if (v.isBoolean()) return v.asBoolean();
        String s = v.asText();
        if (s == null) return null;
        return switch (s.trim().toLowerCase()) {
            case "y","yes","true","1","t" -> true;
            case "n","no","false","0","f" -> false;
            default -> null;
        };
    }

    private Timestamp ts(JsonNode n, String field) {
        String s = text(n, field);
        if (s == null || s.isBlank()) return null;
        try {
            return Timestamp.from(OffsetDateTime.parse(s).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long longOrNull(JsonNode n, String field) {
        String s = text(n, field);
        if (s == null) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            // v2 payloads use GUIDs; your DDL requires bigint -> caller must decide how to handle
            return null;
        }
    }

    private String json(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }

    private void setInt(java.sql.PreparedStatement ps, int idx, Integer v) throws java.sql.SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.INTEGER); else ps.setInt(idx, v);
    }
    private void setDouble(java.sql.PreparedStatement ps, int idx, Double v) throws java.sql.SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.DOUBLE); else ps.setDouble(idx, v);
    }
    private void setBoolean(java.sql.PreparedStatement ps, int idx, Boolean v) throws java.sql.SQLException {
        if (v == null) ps.setNull(idx, java.sql.Types.BIT); else ps.setBoolean(idx, v);
    }
}
