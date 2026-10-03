package com.nexturn.vehicleregistration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.AuditLogs;

public interface AuditLogRepository extends JpaRepository<AuditLogs, Long> {
}
