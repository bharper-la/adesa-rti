package com.la.dataservices.adesa_rti;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

/**
 * Minimal CloudEvents v1.0 representation for JSON "structured mode" batches.
 * See: https://cloudevents.io/
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CloudEvent {
    private String id;
    private String source;          // URI-reference in spec
    private String type;
    private String specversion;     // e.g., "1.0"
    private String subject;         // optional
    private String time;            // RFC3339 string
    private String datacontenttype; // optional
    private String dataschema;      // optional
    private JsonNode data;          // event payload
}
