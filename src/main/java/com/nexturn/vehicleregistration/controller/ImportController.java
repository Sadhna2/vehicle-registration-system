package com.nexturn.vehicleregistration.controller;

import com.nexturn.vehicleregistration.dto.response.ImportResultResponse;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.security.Actor;
import com.nexturn.vehicleregistration.security.Allow;
import com.nexturn.vehicleregistration.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * CSV uploads. Call with {@code dryRun=true} (the default) to check the file and see row errors,
 * then with {@code dryRun=false} to save; nothing is saved if any row has an error.
 */
@RestController
@RequestMapping("/api/import")
public class ImportController {
    @Autowired
    private ImportService importService;

    @PostMapping(value = "/fees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Allow(SessionRole.RTO_ADMIN)
    public ImportResultResponse fees(@RequestPart("file") MultipartFile file,
            @RequestParam(name = "dryRun", defaultValue = "true") boolean dryRun) {
        return importService.importFeeRules(file, dryRun);
    }

    @PostMapping(value = "/employees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Allow({SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN})
    public ImportResultResponse employees(Actor actor, @RequestPart("file") MultipartFile file,
            @RequestParam(name = "dryRun", defaultValue = "true") boolean dryRun) {
        return importService.importEmployees(file, dryRun, actor.role());
    }
}
