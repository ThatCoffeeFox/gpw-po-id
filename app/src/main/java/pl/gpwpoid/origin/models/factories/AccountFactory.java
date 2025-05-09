package pl.gpwpoid.origin.models.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.keys.AccountInfoId;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class AccountFactory {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Account createAccount(
            String email,
            String unprotectedPassword,
            String firstName,
            String secondaryName,
            String lastName,
            PostalCodesTowns postalCodesTowns,
            String phoneNumber,
            String pesel,
            String street,
            String streetNumber,
            String apartmentNumber,
            Account.UserRole role
    ) {
        if(email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if(unprotectedPassword == null || unprotectedPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if(phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }


        Account account = new Account();
        account.setRole(role != null ? role : Account.UserRole.user);

        AccountInfo accountInfo = new AccountInfo();

        AccountInfoId accountInfoId = new AccountInfoId();
        accountInfoId.setUpdatedAt(new Date());
        accountInfo.setId(accountInfoId);

        accountInfo.setAccount(account);
        accountInfo.setFirstName(firstName);
        accountInfo.setSecondaryName(secondaryName);
        accountInfo.setLastName(lastName);
        accountInfo.setStreet(street);
        accountInfo.setStreetNumber(streetNumber);
        accountInfo.setApartmentNumber(apartmentNumber);
        accountInfo.setPhoneNumber(phoneNumber);
        accountInfo.setPesel(pesel);
        accountInfo.setEmail(email);
        accountInfo.setPassword(passwordEncoder.encode(unprotectedPassword));
        accountInfo.setPostalCodesTowns(postalCodesTowns);

        Set<AccountInfo> infos = new HashSet<>();
        infos.add(accountInfo);
        account.setAccountInfos(infos);

        return account;
    }
}
