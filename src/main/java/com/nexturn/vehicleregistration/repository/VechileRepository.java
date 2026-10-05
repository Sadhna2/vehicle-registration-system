package com.nexturn.vehicleregistration.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nexturn.vehicleregistration.entity.Vehicle;
import jakarta.persistence.LockModeType;
public interface VechileRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCurrentOwnerOwnerId(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.temporaryregisterNo = :id")
    Optional<Vehicle> locked(@Param("id") Long id);

    long countByRegistrationValidTillLessThanEqual(LocalDate deadline);
}
