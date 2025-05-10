package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.factories.AccountFactory;
import pl.gpwpoid.origin.repositories.AccountRepository;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.services.AccountService;

import java.util.Collection;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountFactory accountFactory;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, AccountFactory accountFactory) {
        this.accountRepository = accountRepository;
        this.accountFactory = accountFactory;
    }

    @Override
    @Transactional
    public Account addAccount(String email,
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
        Account newAccount = accountFactory.createAccount(email, unprotectedPassword, firstName, secondaryName, lastName, postalCodesTowns, phoneNumber, pesel, street, streetNumber, apartmentNumber, role);
        return accountRepository.save(newAccount);
    }

    @Override
    @Transactional
    public void updateAccountById(Long id) {
        if(id == null || !accountRepository.existsById(id)) {
            throw new IllegalArgumentException("ID nie istnieje lub nie może być null");
        }

        Account account = accountRepository.findById(id).isPresent() ? accountRepository.findById(id).get() : null;
        if(account == null) {
            throw new NullPointerException("Nie znaleziono konta o takim ID");
        }

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccountById(Long id) {
        if(id == null || !accountRepository.existsById(id)) {
            throw new IllegalArgumentException("ID nie istnieje lub nie może być null");
        }

        Account account = accountRepository.findById(id).isPresent() ? accountRepository.findById(id).get() : null;
        if(account == null) {
            throw new NullPointerException("Nie znaleziono konta o takim ID");
        }

        accountRepository.delete(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Account> getAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<AccountListItem> getAccountViewList() {
        return accountRepository.findAllAccountsAsViewItems();
    }
}
