package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.RegistrationCertificateResponse;
import com.nexturn.vehicleregistration.entity.RegistrationCertificate;

public final class RegistrationCertificateMapper {

    private RegistrationCertificateMapper() {
    }

    public static RegistrationCertificateResponse toResponse(
            RegistrationCertificate certificate) {

        if (certificate == null) {
            return null;
        }

        String applicationRefNo = null;
        Long employeeId = null;
        String employeeName = null;
        Long ownerId = null;
        String ownerName = null;

        if (certificate.getApplication() != null) {
            applicationRefNo =
                    certificate.getApplication().getApplicationRefNo();
        }

        if (certificate.getIssuedBy() != null) {
            employeeId = certificate.getIssuedBy().getEmployeeId();

            employeeName =
                    certificate.getIssuedBy().getFirstName()
                    + " "
                    + certificate.getIssuedBy().getLastName();
        }

        if (certificate.getRegisteredOwner() != null) {
            ownerId = certificate.getRegisteredOwner().getOwnerId();

            ownerName =
                    certificate.getRegisteredOwner().getFirstName()
                    + " "
                    + certificate.getRegisteredOwner().getLastName();
        }

        return new RegistrationCertificateResponse(
                certificate.getRegistrationNumber(),
                applicationRefNo,
                employeeId,
                employeeName,
                certificate.getIssuedDate(),
                certificate.getValidTill(),
                ownerId,
                ownerName
        );
    }
}