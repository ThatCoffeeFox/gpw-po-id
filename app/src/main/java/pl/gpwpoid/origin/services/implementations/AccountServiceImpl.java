package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.factories.AccountFactory;
import pl.gpwpoid.origin.repositories.AccountRepository;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.ui.views.DTO.RegistrationDTO;

import java.util.Collection;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountFactory accountFactory;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, AccountFactory accountFactory,
                              AddressService addressService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountFactory = accountFactory;
        this.addressService = addressService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Account addAccount(RegistrationDTO registrationDTO) {
        Town town = addressService.getTownById(registrationDTO.getTownId()).get();
        PostalCodesTowns postalCodesTowns = addressService.getPostalCodesTowns(town, registrationDTO.getPostalCode()).get();
        Account newAccount = accountFactory.createAccount(
                registrationDTO.getEmail(),
                registrationDTO.getPassword(),
                registrationDTO.getFirstName(),
                registrationDTO.getSecondaryName(),
                registrationDTO.getLastName(),
                postalCodesTowns,
                registrationDTO.getPhoneNumber(),
                registrationDTO.getPesel(),
                registrationDTO.getStreet(),
                registrationDTO.getStreetNumber(),
                registrationDTO.getApartmentNumber(),
                registrationDTO.getRole()
        );
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
