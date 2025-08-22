package com.la.dataservices.adessa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ImportService {

    private final JdbcTemplate jdbc;

    public ImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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

                // Adjust table/columns for your schema
                jdbc.update("""
          INSERT INTO dbo.EventImports (EventId, EventType, Subject, EventTimeUtc, Payload)
          VALUES (?, ?, ?, ?, ?)
        """, eventId, eventType, subject, ts, d != null ? d.toString() : null);

            } catch (Exception ex) {
                // Consider logging to a dead-letter table for replay
                ex.printStackTrace();
            }
        }
    }
}
