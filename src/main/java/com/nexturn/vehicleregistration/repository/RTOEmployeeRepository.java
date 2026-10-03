package com.nexturn.vehicleregistration.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.RTOEmployee;

public interface RTOEmployeeRepository extends JpaRepository<RTOEmployee, Long> {

	Optional<RTOEmployee> findByEmailAddress(String email);

}
