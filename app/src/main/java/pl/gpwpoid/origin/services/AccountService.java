package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.repositories.views.AccountListItem;

import java.util.Collection;

public interface AccountService {
    Account addAccount(String email,
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
                    Account.UserRole role);
    void updateAccountById(Long id);
    void deleteAccountById(Long id);

    Collection<Account> getAccounts();
    Collection<AccountListItem> getAccountViewList();
}

