package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.ApplicationDetailsResponse;
import com.nexturn.vehicleregistration.entity.*;

public final class ApplicationDetailsMapper {

  private ApplicationDetailsMapper() {}

  public static ApplicationDetailsResponse toResponse(VehicleRegistrationApplication application) {

    return toResponse(application, null, null);
  }

  public static ApplicationDetailsResponse toResponse(
      VehicleRegistrationApplication application,
      OwnerPaymentDetail payment,
      RegistrationCertificate certificate) {
    if (application == null) {
      return null;
    }

    Long vehicleId = null;
    Long applicantId = null;
    Long feeRuleId = null;
    Long verifiedByEmployeeId = null;
    Long inspectedByEmployeeId = null;
    Long decidedByEmployeeId = null;

    if (application.getVehicle() != null) {
      vehicleId = application.getVehicle().getTemporaryregisterNo();
    }

    if (application.getApplicant() != null) {
      applicantId = application.getApplicant().getOwnerId();
    }

    if (application.getFeeRule() != null) {
      feeRuleId = application.getFeeRule().getFeeRuleId();
    }

    if (application.getVerifiedBy() != null) {
      verifiedByEmployeeId = application.getVerifiedBy().getEmployeeId();
    }

    if (application.getInspectedBy() != null) {
      inspectedByEmployeeId = application.getInspectedBy().getEmployeeId();
    }

    if (application.getDecidedBy() != null) {
      decidedByEmployeeId = application.getDecidedBy().getEmployeeId();
    }

    return new ApplicationDetailsResponse(
        application.getApplicationRefNo(),
        vehicleId,
        applicantId,
        application.getApplicationType(),
        application.getApplicationStatus(),
        feeRuleId,
        application.getPayableAmount(),
        application.getPreviousValidUntil(),
        application.getSubmittedDate(),
        application.getUpdatedDate(),
        application.getVerificationRemark(),
        verifiedByEmployeeId,
        application.getVerifiedAt(),
        application.getInspectionScheduleDate(),
        application.getInspectionRemark(),
        inspectedByEmployeeId,
        application.getInspectedAt(),
        decidedByEmployeeId,
        application.getDecidedAt(),
        application.getDecisionRemarks(),
        application.getInspectionStatus(),
        application.getFinalResult(),
        application.getVersion(),
        VehicleMapper.toResponse(application.getVehicle()),
        OwnerMapper.toResponse(application.getApplicant()),
        OwnerPaymentDetailMapper.toResponse(payment),
        RegistrationCertificateMapper.toResponse(certificate));
  }
}

