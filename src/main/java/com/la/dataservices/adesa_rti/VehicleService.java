package com.la.dataservices.adesa_rti;

public interface VehicleService {
    Object upsertFromCloudEvent(CloudEvent evt);
}
