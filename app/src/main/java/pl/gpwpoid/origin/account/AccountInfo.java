package pl.gpwpoid.origin.account;

import jakarta.persistence.*;
import pl.gpwpoid.origin.address.PostalCodeTown;

import java.util.Date;

@Entity
@Table(name = "accounts_info")
public class AccountInfo {

    @EmbeddedId
    private AccountInfoId id;

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "email", nullable = false, length = 256)
    private String email;

    @Column(name = "password", nullable = false, length = 256)
    private String password;

    @Column(name = "first_name", nullable = false, length = 128)
    private String firstName;

    @Column(name = "secondary_name", length = 128)
    private String secondaryName;

    @Column(name = "last_name", nullable = false, length = 256)
    private String lastName;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "postal_code", referencedColumnName = "postal_code"),
            @JoinColumn(name = "town_id", referencedColumnName = "town_id")
    })
    private PostalCodeTown postalCodeTown;

    @Column(name = "street", length = 128)
    private String street;

    @Column(name = "street_number", length = 8)
    private String streetNumber;

    @Column(name = "apartment_number", length = 8)
    private String apartmentNumber;

    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @Column(name = "pesel", length = 11)
    private String pesel;


    public AccountInfoId getId() {
        return id;
    }

    public void setId(AccountInfoId id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public PostalCodeTown getPostalCodeTown() {
        return postalCodeTown;
    }

    public void setPostalCodeTown(PostalCodeTown postalCodeTown) {
        this.postalCodeTown = postalCodeTown;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
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
