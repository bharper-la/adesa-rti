package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final EventImportRepository eventImportRepository;
    private final VehicleService vehicleService;

    public ImportService(EventImportRepository eventImportRepository, VehicleService vehicleService) {
        this.eventImportRepository = eventImportRepository;
        this.vehicleService = vehicleService;
    }

    @Async("importExecutor")
    public void enqueueCloudEvents(List<CloudEvent> events) {
        final String batchId = UUID.randomUUID().toString();
        final int count = events != null ? events.size() : 0;

        // Include batchId in MDC for all logs in this method (even on async thread)
        try (MDC.MDCCloseable c1 = MDC.putCloseable("batchId", batchId)) {
            log.atInfo()
                    .setMessage("Import batch received")
                    .addKeyValue("events", count)
                    .log();

            if (events == null || events.isEmpty()) {
                log.atInfo().setMessage("Import batch empty").log();
                return;
            }

            for (CloudEvent ev : events) {
                final long t0 = System.nanoTime();

                try {
                    // Safe extracts (cheap)
                    final String eventId   = ev != null ? ev.getId() : null;
                    final String eventType = ev != null ? ev.getType() : null;
                    final String subject   = ev != null ? ev.getSubject() : null;

                    // Extract VIN from payload if present (no heavy parsing)
                    String vin = null;
                    JsonNode dataNode = (ev != null) ? ev.getData() : null;
                    if (dataNode != null) {
                        JsonNode vinNode = dataNode.get("vin");
                        if (vinNode != null && !vinNode.isNull()) {
                            vin = vinNode.asText(null);
                        }
                    }

                    // Persist raw event (payload is required; keep, but don't log it)
                    String payload = (dataNode != null) ? dataNode.toString() : null;

                    Timestamp ts = null;
                    try {
                        if (ev != null && ev.getTime() != null) {
                            ts = Timestamp.from(OffsetDateTime.parse(ev.getTime()).toInstant());
                        }
                    } catch (Exception ignored) {
                        // leave ts null if parse fails
                    }

                    log.atInfo()
                            .setMessage("Event enqueue start")
                            .addKeyValue("eventId", eventId)
                            .addKeyValue("type", eventType)
                            .addKeyValue("vin", vin)
                            .log();

                    eventImportRepository.save(
                            EventImport.builder()
                                    .eventId(eventId)
                                    .eventType(eventType)
                                    .subject(subject)
                                    .eventTimeUtc(ts.toInstant())  // if your entity has this; otherwise remove
                                    .payload(payload)
                                    .build()
                    );

                    // Convert payload into domain entity for additional processing
                    vehicleService.upsertFromCloudEvent(ev);

                    //if (applicationContext.getEnvironment().acceptsProfiles(Profiles.of("prod"))) {
                    //    sqlServerStagingWriter.insertFromCloudEvent(ev);
                    //}

                    long durMs = (System.nanoTime() - t0) / 1_000_000;
                    log.atInfo()
                            .setMessage("Event processed")
                            .addKeyValue("eventId", eventId)
                            .addKeyValue("type", eventType)
                            .addKeyValue("vin", vin)
                            .addKeyValue("ms", durMs)
                            .log();

                } catch (Exception ex) {
                    long durMs = (System.nanoTime() - t0) / 1_000_000;
                    log.atError()
                            .setMessage("Event processing failed")
                            .addKeyValue("ms", durMs)
                            .setCause(ex)
                            .log();
                    // TODO: optionally write to a dead-letter table here
                }
            }

            log.atInfo()
                    .setMessage("Import batch completed")
                    .addKeyValue("events", count)
                    .log();
        }
    }
}
