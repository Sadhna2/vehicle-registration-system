package com.nexturn.vehicleregistration.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;
import com.nexturn.vehicleregistration.enums.ApplicationType;
import com.nexturn.vehicleregistration.enums.InspectionStatus;

public record VehicleRegistrationApplicationResponse(
        String reference,
        ApplicationType type,
        ApplicationStatus status,
        BigDecimal amount,
        Instant submittedAt,
        Instant updatedAt,
        Long vehicleId,
        String vehicle,
        String owner,
        VehicleResponse vehicleDetails,
        OwnerResponse applicant,
        String verificationRemark,
        LocalDate inspectionDate,
        InspectionStatus inspectionStatus,
        String inspectionRemark,
        String decisionRemarks,
        boolean paid,
        PaymentResponse payment

) {
}
