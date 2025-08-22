package com.la.dataservices.adesa_rti;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "damage_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DamageItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vehicle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_damage_vehicle")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private VehicleEntity vehicle;

    @Column(length = 512)
    private String description;

    private String photoUrl;
}
