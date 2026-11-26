package com.nexturn.vehicleregistration.service;

/**
 * Downloadable PDF documents. {@code ownerId} is the calling owner's id (only their own
 * applications), or null for RTO staff.
 */
public interface PdfService {

    /** The registration certificate (RC) of an approved application. */
    PdfFile certificate(String referenceNumber, Long ownerId);

    /** The receipt of a successful fee payment. */
    PdfFile receipt(String referenceNumber, Long ownerId);

    /** Department report: application counts, fee collection and the application register. */
    PdfFile applicationsReport();

    record PdfFile(String fileName, byte[] content) {}
}
