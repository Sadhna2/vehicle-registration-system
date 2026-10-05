package com.nexturn.vehicleregistration.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nexturn.vehicleregistration.entity.OwnershipTransferRequest;

public interface OwnershipTransferRequestRepository
        extends JpaRepository<OwnershipTransferRequest, Long> {
    Optional<OwnershipTransferRequest> findByApplicationApplicationRefNo(String ref);

}
