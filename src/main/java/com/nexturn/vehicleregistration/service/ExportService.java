package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.enums.ApplicationStatus;

/**
 * CSV exports. {@code ownerId} scopes owner exports to their own records (null = all, for staff).
 */
public interface ExportService {

    CsvFile applications(Long ownerId, ApplicationStatus status);

    CsvFile vehicles(Long ownerId);

    CsvFile owners();

    CsvFile employees();

    CsvFile feeRules();

    record CsvFile(String fileName, byte[] content) {}
}
