package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.ui.views.DTO.ProfileUpdateDTO;
import pl.gpwpoid.origin.ui.views.DTO.RegistrationDTO;

import java.util.Collection;

public interface AccountService {
    Account addAccount(RegistrationDTO registrationDTO);

    void updateAccount(ProfileUpdateDTO profileUpdateDTO);

    void deleteAccountById(Long id);

    Collection<Account> getAccounts();

    Collection<AccountListItem> getAccountViewList();

    Account getAccountById(Integer id);

    AccountInfo getNewestAccountInfoItemById(Integer id);
}

