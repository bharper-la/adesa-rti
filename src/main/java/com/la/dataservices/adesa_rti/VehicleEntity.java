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
@EqualsAndHashCode(exclude = {
        "damages", "saleChannels", "additionalVehicleImageUrls",
        "sellerAnnouncements", "vehicleParts"
})
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identity
    @Column(length = 64)
    private String externalVehicleId;

    @Column(length = 32)
    private String vin;

    @Column(name = "model_year")
    private Integer year;

    // Core specs
    private String makeName;
    private String modelName;
    private String seriesName;
    private String trim;
    private String bodyStyleName;
    private String engineName;
    private Double engineDisplacement;
    private Integer cylinders;
    private String transmission;

    private String drivetrain;        // accept "drivetrain" or "driveTrain" in service mapper
    private String fuelType;
    private String interiorColor;
    private String exteriorColor;
    private String vehicleType;

    // Odo/location/pricing/dates
    private Integer mileage;
    private String unitOfMeasure;
    private String odometerCondition;
    private String state;
    private String postalCode;
    private Integer currentHighBid;
    private Integer buyNowPrice;

    @Column(length = 64)
    private String auctionEndDate;

    @Column(length = 64)
    private String saleEventDate;

    private String sellerName;

    // Media & links
    @Column(length = 1024)
    private String primaryImageUrl;

    @Column(length = 1024)
    private String vehicleDetailUrl;

    @Column(length = 1024)
    private String imageViewerUrl;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vehicle_sale_channels", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "sale_channel", length = 128)
    @Builder.Default
    private List<String> saleChannels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vehicle_image_urls", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "url", length = 2048)
    @Builder.Default
    private List<String> additionalVehicleImageUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vehicle_seller_announcements", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "announcement", length = 512)
    @Builder.Default
    private List<String> sellerAnnouncements = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vehicle_parts", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "part", length = 128)
    @Builder.Default
    private List<String> vehicleParts = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vehicle_tires", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "tire", length = 256)
    @Builder.Default
    private List<String> tires = new ArrayList<>();

    // Auction/vendor/listing meta
    private Boolean hasPriorPaintwork;
    private String sellerType;
    private String lotNumber;
    private String runNumber;
    private String lane;
    private String auctionVendor;
    private String assetType;
    private String saleEventType;

    private Boolean openEqualsReserve;
    private String rlLbListingCategory; // handles rlLbListingCategory/rlLBListingCategory from payload

    // Condition / grading
    private Double  vehicleGrade;
    private Boolean isAutoGrade;
    private String  titleState;
    private Integer frameDamage;

    // Inspection / condition details
    @Column(length = 64)
    private String inspectionDate;
    private String inspectionCompany;

    @Column(length = 1024)
    private String inspectionComments;

    private String structuralDamage;

    // Some sources use "drivable" vs "driveable"; service maps both to this
    private String drivable;

    private String odors;
    private String keys;
    private String remotes;
    private String manuals;

    // Status flags
    private Boolean inTransitIndicator;
    private Boolean adesaAssurance;
    private String  autoCheck;

    private Boolean firstRun;
    private Boolean lastRun;
    private Boolean pdi;
    private Boolean psi;
    private Boolean priceReduced;
    private Boolean thirdPartyCRExists;

    // Misc IDs & times
    private Integer country;
    private Long    saleEventId;

    @Column(length = 64)
    private String autobidStartTime;

    private Boolean psiEligibilityAutobid;

    @Column(length = 64)
    private String priceUpdated;

    private String stockNumber;
    @Column(length = 1024)
    private String highValueOptions;

    private Integer doors;
    @Column(length = 1024)
    private String statements;

    private String smokedIn;
    private String testDriven;
    private Integer amsSiteId;

    // Damages summary + items
    private Integer damagesCount;
    private Integer damagesAmount;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DamageItemEntity> damages = new ArrayList<>();
}
