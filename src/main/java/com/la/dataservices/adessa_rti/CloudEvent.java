package com.la.dataservices.adessa_rti;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Minimal CloudEvents v1.0 representation for JSON "structured mode" batches.
 * See: https://cloudevents.io/
 */
public class CloudEvent {
    private String id;
    private String source;         // URI-reference in spec
    private String type;
    private String specversion;    // e.g., "1.0"
    private String subject;        // optional
    private String time;           // RFC3339 string
    private String datacontenttype;// optional
    private String dataschema;     // optional
    private JsonNode data;         // your custom payload

    public CloudEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSpecversion() { return specversion; }
    public void setSpecversion(String specversion) { this.specversion = specversion; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDatacontenttype() { return datacontenttype; }
    public void setDatacontenttype(String datacontenttype) { this.datacontenttype = datacontenttype; }

    public String getDataschema() { return dataschema; }
    public void setDataschema(String dataschema) { this.dataschema = dataschema; }

    public com.fasterxml.jackson.databind.JsonNode getData() { return data; }
    public void setData(com.fasterxml.jackson.databind.JsonNode data) { this.data = data; }
}
