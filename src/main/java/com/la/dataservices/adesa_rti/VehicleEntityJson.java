package com.la.dataservices.adesa_rti;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleEntityJson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String externalVehicleId;

    @Column(length = 32)
    private String vin;

    @Column(name = "model_year")
    private Integer year;

    private String makeName;
    private String modelName;
    private String seriesName;
    private String bodyStyleName;
    private String engineName;
    private Double engineDisplacement;
    private Integer cylinders;
    private String transmission;
    private String drivetrain;
    private String fuelType;
    private String interiorColor;
    private String exteriorColor;
    private String vehicleType;
    private Integer mileage;
    private String unitOfMeasure;
    private String odometerCondition;
    private String state;
    private String postalCode;
    private Integer currentHighBid;
    private Integer buyNowPrice;
    private String auctionEndDate;
    private String sellerName;

    // Legacy JSON-string fields
    @Lob @Column(name = "additional_vehicle_image_urls")
    private String additionalVehicleImageUrlsJson;

    @Lob @Column(name = "seller_announcements")
    private String sellerAnnouncementsJson;

    @Lob @Column(name = "vehicle_parts")
    private String vehiclePartsJson;

    @Lob @Column(name = "runlist_dealer_codes")
    private String runlistDealerCodesJson;

    @Lob @Column(name = "liveblock_dealer_codes")
    private String liveblockDealerCodesJson;

    @Lob @Column(name = "dealerblock_dealer_codes")
    private String dealerblockDealerCodesJson;

    @Lob @Column(name = "damages")
    private String damagesJson;

    @Lob @Column(name = "tires")
    private String tiresJson;
}
