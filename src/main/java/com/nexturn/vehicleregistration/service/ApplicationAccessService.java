package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;

public interface ApplicationAccessService {

    VehicleRegistrationApplication readable(
            String referenceNumber, Actor actor);

    void ownerApplication(
            VehicleRegistrationApplication application, Actor actor);
}