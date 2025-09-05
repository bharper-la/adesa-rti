package com.la.dataservices.adesa_rti;

/**
 * Abstraction for writing event imports to a staging store (e.g., SQL Server).
 * In dev/test, use NoopStagingWriter; in prod, provide a real implementation.
 */
public interface StagingWriter {
    void writeEventImport(EventImport imp);
}
