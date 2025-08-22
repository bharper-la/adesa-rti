package com.la.dataservices.adesa_rti;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleEntity {
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

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DamageItemEntity> damages = new ArrayList<>();
}
