package pl.gpwpoid.origin.models.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.decorators.validPesel.ValidPesel;
import pl.gpwpoid.origin.models.keys.AccountInfoId;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;

@Entity
@Table(name = "accounts_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    @EmbeddedId
    private AccountInfoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private pl.gpwpoid.origin.models.account.Account account;

    @Email
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "town_id", referencedColumnName = "town_id", nullable = false),
            @JoinColumn(name = "postal_code", referencedColumnName = "postal_code", nullable = false)
    })
    private PostalCodesTowns postalCodesTowns;

    @Column(name = "street", length = 128)
    private String street;

    @Column(name = "street_number", length = 8)
    private String streetNumber;

    @Column(name = "apartment_number", length = 8)
    private String apartmentNumber;

    @Pattern(regexp = "\\+[0-9]{10,13}")
    @Column(name = "phone_number", nullable = false, length = 16)
    private String phoneNumber;

    @ValidPesel
    @Column(name = "pesel", length = 11)
    private String pesel;
}