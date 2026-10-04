package com.nexturn.vehicleregistration.service.impl;

import static com.nexturn.vehicleregistration.service.WorkflowSupport.ensure;

import com.nexturn.vehicleregistration.auth.Access;
import com.nexturn.vehicleregistration.auth.Actor;
import com.nexturn.vehicleregistration.auth.Passwords;
import com.nexturn.vehicleregistration.dto.request.AccountRequest;
import com.nexturn.vehicleregistration.dto.request.RTOEmployeeRequest;
import com.nexturn.vehicleregistration.dto.response.OwnerResponse;
import com.nexturn.vehicleregistration.dto.response.RTOEmployeeResponse;
import com.nexturn.vehicleregistration.entity.Owner;
import com.nexturn.vehicleregistration.entity.RTOEmployee;
import com.nexturn.vehicleregistration.enums.RTOEmployeeRole;
import com.nexturn.vehicleregistration.enums.SessionRole;
import com.nexturn.vehicleregistration.exception.DuplicateRecordException;
import com.nexturn.vehicleregistration.exception.EmployeeNotFoundException;
import com.nexturn.vehicleregistration.exception.InvalidRequestException;
import com.nexturn.vehicleregistration.exception.OwnerNotFoundException;
import com.nexturn.vehicleregistration.mapper.OwnerMapper;
import com.nexturn.vehicleregistration.mapper.RTOEmployeeMapper;
import com.nexturn.vehicleregistration.repository.OwnerRepository;
import com.nexturn.vehicleregistration.repository.RTOEmployeeRepository;
import com.nexturn.vehicleregistration.service.AuditService;
import com.nexturn.vehicleregistration.service.RTOEmployeeService;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RTOEmployeeServiceImpl implements RTOEmployeeService {

    private final OwnerRepository ownerRepository;
    private final RTOEmployeeRepository employeeRepository;
    private final Access access;
    private final AuditService auditService;

    public RTOEmployeeServiceImpl(
            OwnerRepository ownerRepository,
            RTOEmployeeRepository employeeRepository,
            Access access,
            AuditService auditService) {

        this.ownerRepository = ownerRepository;
        this.employeeRepository = employeeRepository;
        this.access = access;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RTOEmployeeResponse> employees(Actor actor) {

        access.require(actor, SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN);

        return employeeRepository.findAll()
                .stream()
                .map(RTOEmployeeMapper::toResponse)
                .toList();
    }

    @Override
    public RTOEmployeeResponse employee(
            RTOEmployeeRequest request, Actor actor) {

        access.require(actor, SessionRole.RTO_ADMIN, SessionRole.SYSTEM_ADMIN);

        if (actor.role() == SessionRole.RTO_ADMIN) {
            ensure(
                    request.role() == RTOEmployeeRole.RTO_OFFICER,
                    "Only system admin can create administrators");
        }

        String email = request.emailAddress()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (employeeRepository.findByEmailAddress(email).isPresent()) {
            throw new DuplicateRecordException(
                    "Employee email is already registered");
        }

        RTOEmployee employee = new RTOEmployee();

        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmailAddress(email);
        employee.setPassword(Passwords.hash(request.password()));
        employee.setPhoneNumber(request.phoneNumber());
        employee.setDesignation(request.designation());
        employee.setRole(request.role());

        RTOEmployee savedEmployee = employeeRepository.save(employee);

        auditService.record(
                actor,
                null,
                "EMPLOYEE_CREATED",
                savedEmployee.getEmailAddress());

        return RTOEmployeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerResponse> owners(Actor actor) {

        access.require(actor, SessionRole.SYSTEM_ADMIN);

        return ownerRepository.findAll()
                .stream()
                .map(OwnerMapper::toResponse)
                .toList();
    }

    @Override
    public void account(
            String accountType,
            Long accountId,
            AccountRequest request,
            Actor actor) {

        access.require(actor, SessionRole.SYSTEM_ADMIN);

        if ("owners".equals(accountType)) {
            updateOwner(accountId, request);

        } else if ("employees".equals(accountType)) {
            updateEmployee(accountId, request, actor);

        } else {
            throw new InvalidRequestException("Unknown account type");
        }

        auditService.record(
                actor,
                null,
                "ACCOUNT_UPDATED",
                accountType + ":" + accountId);
    }

    private void updateOwner(Long ownerId, AccountRequest request) {

        ensure(
                request.role() == null,
                "Owners have no database role field");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        owner.setStatus(request.status());
        owner.setDateOfUpdate(Instant.now());
    }

    private void updateEmployee(
            Long employeeId,
            AccountRequest request,
            Actor actor) {

        ensure(
                !employeeId.equals(actor.id()),
                "Cannot modify your own administrator account");

        RTOEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (request.role() != null) {
            employee.setRole(request.role());
        }

        employee.setStatus(request.status());
    }
}