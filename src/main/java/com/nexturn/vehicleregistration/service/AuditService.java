package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

public interface AuditService {

    void record(
            Actor actor,
            VehicleRegistrationApplication application,
            String action,
            String remarks);
}
