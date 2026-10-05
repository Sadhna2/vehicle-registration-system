package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

public interface AuditService {

    void record(
            VehicleRegistrationApplication application,
            String action,
            String remarks);
}
