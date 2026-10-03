package com.nexturn.vehicleregistration.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmailAddress(String email);

}

