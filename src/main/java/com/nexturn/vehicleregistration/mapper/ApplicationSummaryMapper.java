package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.ApplicationSummaryResponse;
import com.nexturn.vehicleregistration.entity.VehicleRegistrationApplication;


public final class ApplicationSummaryMapper {
  private ApplicationSummaryMapper() {}

  public static ApplicationSummaryResponse toSummary(VehicleRegistrationApplication a) {
    return new ApplicationSummaryResponse(
        a.getApplicationRefNo(),
        a.getApplicationType(),
        a.getApplicationStatus(),
        a.getPayableAmount(),
        a.getSubmittedDate(),
        a.getUpdatedDate(),
        a.getVehicle().getTemporaryregisterNo(),
        a.getVehicle().getManufacturerName() + " " + a.getVehicle().getModelName(),
        a.getApplicant().getFirstName() + " " + a.getApplicant().getLastName());
  }
}

