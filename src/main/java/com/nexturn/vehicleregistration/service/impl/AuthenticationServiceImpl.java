package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.auth.Passwords;
import com.nexturn.vehicleregistration.dto.request.LoginRequest;
import com.nexturn.vehicleregistration.dto.request.OwnerRequest;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.DuplicateRecordException;
import com.nexturn.vehicleregistration.exception.InvalidLoginException;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.service.AuthenticationService;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    private final OwnerRepository ownerRepository;
    private final RTOEmployeeRepository employeeRepository;

    public AuthenticationServiceImpl(
            OwnerRepository ownerRepository,
            RTOEmployeeRepository employeeRepository) {

        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Actor signup(OwnerRequest request) {

        String email = request.emailAddress()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (ownerRepository.findByEmailAddress(email).isPresent()) {
            throw new DuplicateRecordException(
                    "Owner email is already registered");
        }

        Owner owner = new Owner();

        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setEmailAddress(email);
        owner.setPhoneNumber(request.phoneNumber());
        owner.setPassword(Passwords.hash(request.password()));
        owner.setDateOfBirth(request.dateOfBirth());
        owner.setIdentityProofType(request.identityProofType());
        owner.setIdentityProofNumber(request.identityProofNumber());
        owner.setAddress(request.address());
        owner.setCityName(request.cityName());
        owner.setStateName(request.stateName());
        owner.setPincode(request.pincode());

        Owner savedOwner = ownerRepository.save(owner);

        return new Actor(
                savedOwner.getOwnerId(),
                SessionRole.OWNER,
                savedOwner.getFirstName());
    }

    @Override
    public Actor login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (request.staff()) {
            return loginEmployee(email, request.password());
        }

        return loginOwner(email, request.password());
    }

    private Actor loginEmployee(String email, String password) {

        RTOEmployee employee = employeeRepository
                .findByEmailAddress(email)
                .orElseThrow(this::invalidLogin);

        if (employee.getStatus() != AccountStatus.ACTIVE
                || !Passwords.matches(password, employee.getPassword())) {

            throw invalidLogin();
        }

        return new Actor(
                employee.getEmployeeId(),
                SessionRole.valueOf(employee.getRole().name()),
                employee.getFirstName());
    }

    private Actor loginOwner(String email, String password) {

        Owner owner = ownerRepository
                .findByEmailAddress(email)
                .orElseThrow(this::invalidLogin);

        if (owner.getStatus() != AccountStatus.ACTIVE
                || !Passwords.matches(password, owner.getPassword())) {

            throw invalidLogin();
        }

        return new Actor(
                owner.getOwnerId(),
                SessionRole.OWNER,
                owner.getFirstName());
    }

    private InvalidLoginException invalidLogin() {
        return new InvalidLoginException(
                "Invalid credentials or inactive account");
    }
}
