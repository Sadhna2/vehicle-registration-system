package com.nexturn.vehicleregistration.entity;
import com.nexturn.vehicleregistration.enums.AccountStatus;
import com.nexturn.vehicleregistration.enums.IdentityProofType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "owner_details")
@Getter
@Setter
@NoArgsConstructor
public class Owner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "owner_id")
	private Long ownerId;
	
	@Column(name = "first_name", nullable = false, length = 30)
    private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_proof_type", nullable = false, length = 30)
    private IdentityProofType identityProofType;
    @Column(name = "identity_proof_number", nullable = false, unique = true, length = 30)
    private String identityProofNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OwnerAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "owner")
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "owner")
    private List<RegistrationApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "currentOwner")
    private List<OwnershipTransfer> transfersGiven = new ArrayList<>();

    @OneToMany(mappedBy = "newOwner")
    private List<OwnershipTransfer> transfersReceived = new ArrayList<>();
    
    public void addAddress(OwnerAddress address) {
        addresses.add(address);
        address.setOwner(this);
    }

}
