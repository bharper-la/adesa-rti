package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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
@Profile("!prod")
@RequiredArgsConstructor
public class H2StagingWriter implements StagingWriter {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    private static final String INSERT_SQL = """
    INSERT INTO ADESA_JSON_RTI (
        eventType, eventDate, vehicleId, vin, sellerName, "year",
        makeName, modelName, seriesName, bodyStyleName, engineName, engineDisplacement, cylinders,
        transmission, drivetrain, fuelType, interiorColor, exteriorColor, vehicleType, mileage,
        unitOfMeasure, odometerCondition, "state", postalCode, currentHighBid, buyNowPrice,
        auctionEndDate, primaryImageUrl, vehicleDetailUrl, imageViewerUrl,
        additionalVehicleImageUrls, sellerAnnouncements, vehicleParts,
        hasPriorPaintwork, sellerType, lotNumber, runNumber, lane, auctionVendor,
        runlistDealerCodes, liveblockDealerCodes, dealerblockDealerCodes, assetType,
        saleEventType, saleEventDate, damagesCount, damagesAmount, damagesItems,
        openEqualsReserve, vehicleGrade, isAutoGrade, titleState, frameDamage, tires
    ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """;

    @Override
    public void writeEventImport(EventImport imp) {
        // imp.getPayload() is the raw JSON string; we parse to pull the fields
        try {
            JsonNode d = mapper.readTree(Objects.requireNonNull(imp.getPayload(), "payload is null"));

            String eventType = text(d, "eventType");
            Timestamp eventDate = ts(d, "eventDate");
            Long vehicleId = longOrNull(d, "vehicleId"); // null for GUIDs
            String vin = text(d, "vin");

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
            String drivetrain            = text(d, "driveTrain"); // payload uses driveTrain; column is drivetrain
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
            Timestamp auctionEndDate     = ts(d, "auctionEndDate");
            String primaryImageUrl       = text(d, "primaryImageUrl");
            String vehicleDetailUrl      = text(d, "vehicleDetailUrl");
            String imageViewerUrl        = text(d, "imageViewerUrl");

            String jsonAdditionalImages  = json(d.get("additionalVehicleImageUrls"));
            String jsonSellerAnnouncements = json(d.get("sellerAnnouncements"));
            String jsonVehicleParts      = json(d.get("vehicleParts"));

            Boolean hasPriorPaintwork    = boolOrNull(d, "hasPriorPaintwork");
            String sellerType            = text(d, "sellerType");
            String lotNumber             = text(d, "lotNumber");
            String runNumber             = text(d, "runNumber");
            String lane                  = text(d, "lane");
            String auctionVendor         = text(d, "auctionVendor");

            String jsonRunlistCodes      = json(d.get("runlistDealerCodes"));
            String jsonLiveblockCodes    = json(d.get("liveblockDealerCodes"));
            String jsonDealerblockCodes  = json(d.get("dealerblockDealerCodes"));
            String assetType             = text(d, "assetType");

            String saleEventType         = text(d, "saleEventType");
            Timestamp saleEventDate      = ts(d, "saleEventDate");

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
            Integer frameDamage          = intOrNull(d, "frameDamage");
            String jsonTires             = json(d.get("tires"));

            jdbc.update(INSERT_SQL, ps -> {
                int i = 1;
                ps.setString(i++, eventType);
                ps.setTimestamp(i++, eventDate);
                setLong(ps, i++, vehicleId);
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

            log.debug("H2 ADESA_JSON insert OK: eventId={}", imp.getEventId());

        } catch (Exception e) {
            log.error("H2 staging insert failed: eventId={}", imp.getEventId(), e);
            throw new RuntimeException(e);
        }
    }

    // --- small helpers ---
    private String text(JsonNode n, String f){ return n!=null&&n.has(f)&&!n.get(f).isNull()?n.get(f).asText():null; }
    private Integer intOrNull(JsonNode n,String f){ return n!=null&&n.has(f)&&!n.get(f).isNull()?n.get(f).asInt():null; }
    private Double doubleOrNull(JsonNode n,String f){ return n!=null&&n.has(f)&&!n.get(f).isNull()?n.get(f).asDouble():null; }
    private Boolean boolOrNull(JsonNode n,String f){
        if(n==null||!n.has(f)||n.get(f).isNull()) return null;
        JsonNode v=n.get(f); if(v.isBoolean()) return v.asBoolean();
        String s=v.asText(); if(s==null) return null;
        return switch(s.trim().toLowerCase()){case"y","yes","true","1","t"->true;case"n","no","false","0","f"->false;default->null;};
    }
    private int safeInt(JsonNode v){ try{ return v.isInt()?v.asInt():Integer.parseInt(v.asText()); }catch(Exception e){ return 0; } }
    private Timestamp ts(JsonNode n,String f){ String s=text(n,f); if(s==null||s.isBlank())return null; try{ return Timestamp.from(OffsetDateTime.parse(s).toInstant()); }catch(Exception e){return null;} }
    private Long longOrNull(JsonNode n,String f){ String s=text(n,f); if(s==null) return null; try{ return Long.parseLong(s);}catch(NumberFormatException e){ return null; } }
    private String json(JsonNode node){ if(node==null||node.isNull()) return null; try{ return mapper.writeValueAsString(node);}catch(Exception e){ return null; } }
    private void setInt(java.sql.PreparedStatement ps,int i,Integer v)throws java.sql.SQLException{ if(v==null)ps.setNull(i,java.sql.Types.INTEGER); else ps.setInt(i,v); }
    private void setLong(java.sql.PreparedStatement ps,int i,Long v)throws java.sql.SQLException{ if(v==null)ps.setNull(i,java.sql.Types.BIGINT); else ps.setLong(i,v); }
    private void setDouble(java.sql.PreparedStatement ps,int i,Double v)throws java.sql.SQLException{ if(v==null)ps.setNull(i,java.sql.Types.DOUBLE); else ps.setDouble(i,v); }
    private void setBoolean(java.sql.PreparedStatement ps,int i,Boolean v)throws java.sql.SQLException{ if(v==null)ps.setNull(i,java.sql.Types.BIT); else ps.setBoolean(i,v); }
}
