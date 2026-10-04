package com.nexturn.vehicleregistration.config;

import com.nexturn.vehicleregistration.auth.Passwords;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.enums.RTOEmployeeDesignation;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;
import com.nexturn.vehicleregistration.exception.InvalidRequestException;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import jakarta.validation.Validator;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdministratorBootstrapConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "vrs.bootstrap-admin.enabled",
            havingValue = "true"
    )
    CommandLineRunner firstAdministrator(
            RTOEmployeeRepository employeeRepository,
            Validator validator,
            @Value("${BOOTSTRAP_EMAIL:}") String email,
            @Value("${BOOTSTRAP_PASSWORD:}") String password,
            @Value("${BOOTSTRAP_PHONE:}") String phone) {

        return args -> {
            if (employeeRepository.count() > 0) {
                return;
            }

            RTOEmployeeRequest request = new RTOEmployeeRequest(
                    "System",
                    "Administrator",
                    email.trim().toLowerCase(Locale.ROOT),
                    phone,
                    password,
                    RTOEmployeeDesignation.ADMIN,
                    RTOEmployeeRole.SYSTEM_ADMIN
            );

            if (!validator.validate(request).isEmpty()) {
                throw new InvalidRequestException(
                        "Set a valid BOOTSTRAP_EMAIL, BOOTSTRAP_PHONE, and "
                                + "BOOTSTRAP_PASSWORD containing 10 to 128 characters"
                );
            }

            RTOEmployee administrator = new RTOEmployee();
            administrator.setFirstName(request.firstName());
            administrator.setLastName(request.lastName());
            administrator.setEmailAddress(request.emailAddress());
            administrator.setPhoneNumber(request.phoneNumber());
            administrator.setPassword(Passwords.hash(request.password()));
            administrator.setDesignation(request.designation());
            administrator.setRole(request.role());

            employeeRepository.save(administrator);
        };
    }
}