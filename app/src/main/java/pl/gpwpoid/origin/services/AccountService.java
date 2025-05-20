package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.ui.views.DTO.RegistrationDTO;

import java.util.Collection;

public interface AccountService {
    Account addAccount(RegistrationDTO registrationDTO);
    void updateAccountById(Long id);
    void deleteAccountById(Long id);

    Collection<Account> getAccounts();
    Collection<AccountListItem> getAccountViewList();
    Account getAccountById(Integer id);
    AccountListItem getNewestAccountInfoById(Integer id);
}

