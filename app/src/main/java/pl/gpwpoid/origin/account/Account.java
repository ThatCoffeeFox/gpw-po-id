package pl.gpwpoid.origin.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

enum UserRole {
    admin,
    user
}

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @NotNull 
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE DEFAULT current_timestamp")
    
    private LocalDateTime createdAt;

    @NotBlank 
    @Email(regexp = ".+@.+\\.[a-z]+") 
    @Size(max = 256)
    @Column(name = "email", length = 256, unique = true, nullable = false)
    private String email;

    @NotBlank 
    @Size(min = 8, max = 256) 
    @Column(name = "password", length = 256, nullable = false)
    
    private String password; 

    @NotNull
    @Enumerated(EnumType.STRING) 
    @Column(name = "role", nullable = false, columnDefinition = "user_role") 
    private UserRole role;

    @NotBlank
    @Size(max = 128)
    @Column(name = "first_name", length = 128, nullable = false)
    private String firstName;

    @Size(max = 128) 
    @Column(name = "secondary_name", length = 128)
    private String secondaryName;

    @NotBlank
    @Size(max = 256)
    @Column(name = "last_name", length = 256, nullable = false)
    private String lastName;

    @NotNull
    @Column(name = "town_id", nullable = false)
    private Integer townId;

    @NotBlank
    @Size(max = 6) 
    @Column(name = "postal_code", length = 6, nullable = false)
    private String postalCode;

    @Size(max = 128) 
    @Column(name = "street", length = 128)
    private String street;

    @Positive 
    @Column(name = "street_number") 
    private Integer streetNumber;

    @Size(max = 8) 
    @Column(name = "apartment_number", length = 8)
    private String apartmentNumber;

    @NotBlank
    @Pattern(regexp = "\\+[0-9]{10,13}", message = "Phone number must start with + and have 10-13 digits")
    @Size(max = 16)
    @Column(name = "phone_number", length = 16, nullable = false)
    private String phoneNumber;

    
    @Size(min = 11, max = 11, message = "PESEL must be exactly 11 digits")
    @Pattern(regexp = "[0-9]{11}", message = "PESEL must contain only digits")
    
    @Column(name = "pesel", length = 11, unique = true) 
    private String pesel;

    /*
    @ManyToOne(fetch = FetchType.LAZY) // LAZY is generally preferred
    @JoinColumns({
        @JoinColumn(name = "town_id", referencedColumnName = "town_id", insertable = false, updatable = false),
        @JoinColumn(name = "postal_code", referencedColumnName = "postal_code", insertable = false, updatable = false)
    })
    private PostalCodeTown postalCodeTown; // Assumes a PostalCodeTown entity exists
    */

    public Account() {

    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondaryName() {
        return secondaryName;
    }

    public void setSecondaryName(String secondaryName) {
        this.secondaryName = secondaryName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getTownId() {
        return townId;
    }

    public void setTownId(Integer townId) {
        this.townId = townId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(Integer streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }
}
