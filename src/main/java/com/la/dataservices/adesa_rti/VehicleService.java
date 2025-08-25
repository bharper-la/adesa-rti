package com.la.dataservices.adesa_rti;

public interface VehicleServicePort {
    Object upsertFromCloudEvent(CloudEvent evt);
}
