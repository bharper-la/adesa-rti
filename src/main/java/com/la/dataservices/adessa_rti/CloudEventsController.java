package com.la.dataservices.adessa_rti;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/events")
public class CloudEventsController {

    private final ImportService importService;

    public CloudEventsController(ImportService importService) {
        this.importService = importService;
    }

    /**
     * CloudEvents abuse-protection handshake used by Azure Event Grid when you choose
     * CloudEvents 1.0 schema. Event Grid sends HTTP OPTIONS with WebHook headers.
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> cloudeventsPreflight(
            @RequestHeader(value = "WebHook-Request-Origin", required = false) String origin,
            @RequestHeader(value = "WebHook-Request-Rate", required = false) String rate) {

        // Echo allowed origin; set/echo a reasonable rate
        return ResponseEntity.ok()
                .header("WebHook-Allowed-Origin", origin != null ? origin : "*")
                .header("WebHook-Allowed-Rate", rate != null ? rate : "120")
                .build();
    }

    /**
     * CloudEvents notifications typically arrive as a JSON array with content-type:
     * application/cloudevents-batch+json
     *
     * We also accept application/json for robustness.
     */
    @PostMapping(consumes = { "application/cloudevents-batch+json", MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Void> handleBatch(@RequestBody CloudEvent[] events, HttpServletRequest request) {

        // ACK fast; do work off-thread
        List<CloudEvent> batch = Arrays.asList(events);
        importService.enqueueCloudEvents(batch);

        return ResponseEntity.ok().build();
    }

    /**
     * (Optional) If Azure ever sends single-event structured mode:
     * Content-Type: application/cloudevents+json
     */
    @PostMapping(consumes = "application/cloudevents+json")
    public ResponseEntity<Void> handleSingle(@RequestBody CloudEvent event) {
        importService.enqueueCloudEvents(List.of(event));
        return ResponseEntity.ok().build();
    }
}
