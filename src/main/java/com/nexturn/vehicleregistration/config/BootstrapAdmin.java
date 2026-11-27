package com.nexturn.vehicleregistration.config;

import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.util.PasswordUtil;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Employees can no longer sign themselves up, so the first SYSTEM_ADMIN is created at startup from
 * vrs.bootstrap.admin-email / vrs.bootstrap.admin-password, only when no SYSTEM_ADMIN exists yet.
 */
@Component
public class BootstrapAdmin implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapAdmin.class);

  private final RTOEmployeeRepository employees;
  private final String email;
  private final String password;

  public BootstrapAdmin(
      RTOEmployeeRepository employees,
      @Value("${vrs.bootstrap.admin-email:}") String email,
      @Value("${vrs.bootstrap.admin-password:}") String password) {
    this.employees = employees;
    this.email = email;
    this.password = password;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (employees.existsByRole(RTOEmployeeRole.SYSTEM_ADMIN)) {
      return;
    }
    if (email.isBlank() || password.length() < 10) {
      LOG.warn(
          "No SYSTEM_ADMIN exists. Set VRS_ADMIN_EMAIL and VRS_ADMIN_PASSWORD (10+ characters)"
              + " to create one at startup.");
      return;
    }
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (employees.findByEmailAddress(normalized).isPresent()) {
      LOG.warn("The bootstrap admin email already belongs to an employee; nothing created.");
      return;
    }
    RTOEmployee admin = new RTOEmployee();
    admin.setFirstName("System");
    admin.setLastName("Administrator");
    admin.setEmailAddress(normalized);
    admin.setPhoneNumber("0000000000");
    admin.setPassword(PasswordUtil.hash(password));
    admin.setDesignation(RTOEmployeeDesignation.ADMIN);
    admin.setRole(RTOEmployeeRole.SYSTEM_ADMIN);
    employees.save(admin);
    LOG.info("Created bootstrap SYSTEM_ADMIN account {}", normalized);
  }
}
