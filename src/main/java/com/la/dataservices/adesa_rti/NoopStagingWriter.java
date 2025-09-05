// src/main/java/com/la/dataservices/adesa_rti/NoopStagingWriter.java
package com.la.dataservices.adesa_rti;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@Component
//@Profile("!prod")
public class NoopStagingWriter implements StagingWriter {
    @Override public void writeEventImport(EventImport imp) { /* no-op in dev/test */ }
}
