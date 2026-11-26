package com.nexturn.vehicleregistration.service;

import com.nexturn.vehicleregistration.dto.response.ImportResultResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import org.springframework.web.multipart.MultipartFile;

/**
 * CSV imports of fee rules and employee accounts. Every row is checked with the same rules as the
 * single-record forms. Applications are never imported: they must go through the workflow.
 */
public interface ImportService {

    /** Columns: vehicleCategory, applicationType, feeAmount, effectiveFrom. */
    ImportResultResponse importFeeRules(MultipartFile file, boolean dryRun);

    /** Columns: firstName, lastName, emailAddress, phoneNumber, password, designation, role. */
    ImportResultResponse importEmployees(MultipartFile file, boolean dryRun, SessionRole creatorRole);
}
