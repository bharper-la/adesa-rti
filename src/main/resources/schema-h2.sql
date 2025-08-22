-- schema-h2.sql
CREATE SCHEMA IF NOT EXISTS dbo;
SET SCHEMA dbo;

CREATE TABLE IF NOT EXISTS EventImports (
                                            Id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    EventId        NVARCHAR(200)  NOT NULL,
    EventType      NVARCHAR(200)  NULL,
    Subject        NVARCHAR(400)  NULL,
    EventTimeUtc   DATETIME2(7)   NULL,
    Payload        NVARCHAR(MAX)  NULL,
    -- H2 doesn't have SYSUTCDATETIME(); use CURRENT_TIMESTAMP for demo purposes
    CreatedUtc     DATETIME2(7)   NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS UX_EventImports_EventId
    ON EventImports(EventId);
