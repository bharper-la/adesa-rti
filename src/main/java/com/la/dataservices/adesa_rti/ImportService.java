package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ImportService {

    private final EventImportRepository eventImportRepository;

    private final VehicleService vehicleService;

    public ImportService(EventImportRepository eventImportRepository, VehicleService vehicleService) {
        this.eventImportRepository = eventImportRepository;
        this.vehicleService = vehicleService;
    }

    @Async("importExecutor")
    public void enqueueCloudEvents(List<CloudEvent> events) {
        for (CloudEvent ev : events) {
            try {
                JsonNode d = ev.getData();

                String eventId   = ev.getId();
                String eventType = ev.getType();
                String subject   = ev.getSubject();

                Timestamp ts = null;
                try {
                    if (ev.getTime() != null) {
                        ts = Timestamp.from(OffsetDateTime.parse(ev.getTime()).toInstant());
                    }
                } catch (Exception ignored) { /* leave ts null if parse fails */ }

                eventImportRepository.save(EventImport.builder().eventId(eventId).eventType(eventType).subject(subject).build());

                // Convert payload into domain entity for additional processing
                vehicleService.upsertFromCloudEvent(ev);

            } catch (Exception ex) {
                // Consider logging to a dead-letter table for replay
                ex.printStackTrace();
            }
        }
    }
}
