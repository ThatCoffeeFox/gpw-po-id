package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountListItem {
    private Integer accountId;
    private Date updatedAt;
    private String firstName;
    private String secondaryName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String pesel;
    private String townName;
    private String postalCodeValue;
    private String street;
    private String streetNumber;
    private String apartmentNumber;
}
