package com.la.dataservices.adesa_rti;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/events")
public class CloudEventsController {

    private static final Logger log = LoggerFactory.getLogger(CloudEventsController.class);
    private static final int MAX_IDS_TO_LOG = 3;

    private final ImportService importService;

    public CloudEventsController(ImportService importService) {
        this.importService = importService;
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> cloudeventsPreflight(
            @RequestHeader(value = "WebHook-Request-Origin", required = false) String origin,
            @RequestHeader(value = "WebHook-Request-Rate", required = false) String rate) {

        return ResponseEntity.ok()
                .header("WebHook-Allowed-Origin", origin != null ? origin : "eventgrid.azure.net")
                .header("WebHook-Allowed-Rate", rate != null ? rate : "120")
                .build();
    }

    @PostMapping(consumes = { "application/cloudevents-batch+json", MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Void> handleBatch(@RequestBody CloudEvent[] events, HttpServletRequest request) {

        final int count = events != null ? events.length : 0;
        final long contentLength = request.getContentLengthLong();
        final String clientIp = clientIp(request);
        final String reqId = request.getHeader("X-Request-ID") != null
                ? request.getHeader("X-Request-ID")
                : UUID.randomUUID().toString();

        try (MDC.MDCCloseable c1 = MDC.putCloseable("requestId", reqId);
             MDC.MDCCloseable c2 = MDC.putCloseable("clientIp", clientIp);
             MDC.MDCCloseable c3 = MDC.putCloseable("path", "/events")) {

            // fast, structured log (no string concat)
            log.atInfo()
                    .setMessage("CloudEvents batch received")
                    .addKeyValue("events", count)
                    .addKeyValue("bytes", contentLength)
                    .log();

            if (log.isDebugEnabled() && count > 0) {
                // only compute IDs when debug is on
                var ids = Stream.of(events)
                        .map(CloudEvent::getId)
                        .limit(MAX_IDS_TO_LOG)
                        .toList();
                log.atDebug()
                        .setMessage("Sample event IDs")
                        .addKeyValue("ids", ids)
                        .log();
            }

            // ACK fast; do work off-thread
            List<CloudEvent> batch = Arrays.asList(events);
            importService.enqueueCloudEvents(batch);

            log.atInfo()
                    .setMessage("CloudEvents batch enqueued")
                    .addKeyValue("events", count)
                    .log();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(consumes = "application/cloudevents+json")
    public ResponseEntity<Void> handleSingle(@RequestBody CloudEvent event, HttpServletRequest request) {

        final String clientIp = clientIp(request);
        final String reqId = request.getHeader("X-Request-ID") != null
                ? request.getHeader("X-Request-ID")
                : UUID.randomUUID().toString();

        try (MDC.MDCCloseable c1 = MDC.putCloseable("requestId", reqId);
             MDC.MDCCloseable c2 = MDC.putCloseable("clientIp", clientIp);
             MDC.MDCCloseable c3 = MDC.putCloseable("path", "/events")) {

            log.atInfo()
                    .setMessage("CloudEvent received")
                    .addKeyValue("id", safeId(event))
                    .addKeyValue("type", event != null ? event.getType() : null)
                    .log();

            importService.enqueueCloudEvents(List.of(event));

            log.atInfo()
                    .setMessage("CloudEvent enqueued")
                    .addKeyValue("id", safeId(event))
                    .log();
        }

        return ResponseEntity.ok().build();
    }

    private static String clientIp(HttpServletRequest req) {
        // cheap, common proxies
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        return (realIp != null && !realIp.isBlank()) ? realIp : req.getRemoteAddr();
    }

    private static String safeId(CloudEvent ev) {
        return ev != null ? ev.getId() : null;
    }
}
