package com.la.dataservices.adesa_rti;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "event_imports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventId;
    private String eventType;
    private String subject;
    private Instant eventTimeUtc;
    @Lob private String payload;
}

