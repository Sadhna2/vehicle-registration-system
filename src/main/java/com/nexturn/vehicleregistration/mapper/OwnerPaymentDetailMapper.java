package com.nexturn.vehicleregistration.mapper;

import com.nexturn.vehicleregistration.dto.response.OwnerPaymentDetailResponse;
import com.nexturn.vehicleregistration.entity.OwnerPaymentDetail;

public final class OwnerPaymentDetailMapper {

    private OwnerPaymentDetailMapper() {
    }

    public static OwnerPaymentDetailResponse toResponse(
            OwnerPaymentDetail payment) {

        if (payment == null) {
            return null;
        }

        String applicationRefNo = null;

        if (payment.getApplication() != null) {
            applicationRefNo =
                    payment.getApplication().getApplicationRefNo();
        }

        return new OwnerPaymentDetailResponse(
                payment.getPaymentId(),
                applicationRefNo,
                payment.getAmountPaid(),
                payment.getCardLastFourDigit(),
                payment.getTransactionReferenceId(),
                payment.getPaymentStatus(),
                payment.getPaidAt(),
                payment.getPaymentMethod()
        );
    }
}