package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.factories.AccountFactory;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.repositories.AccountRepository;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.ui.views.DTO.ProfileUpdateDTO;
import pl.gpwpoid.origin.ui.views.DTO.RegistrationDTO;

import java.util.Collection;
import java.util.Optional;

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
    public void updateAccount(ProfileUpdateDTO profileUpdateDTO) {
        if (profileUpdateDTO == null || !accountRepository.existsById(Long.valueOf(profileUpdateDTO.getAccountId()))) {
            throw new IllegalArgumentException("ID nie istnieje lub nie może być null");
        }

        Account account = accountRepository.findById(profileUpdateDTO.getAccountId().longValue()).get();
        Town town = addressService.getTownById(profileUpdateDTO.getTownId()).get();
        PostalCodesTowns postalCodesTowns = addressService.getPostalCodesTowns(town, profileUpdateDTO.getPostalCode()).get();

        AccountInfo updatedAccountInfo = accountFactory.createAccountInfo(profileUpdateDTO, postalCodesTowns);
        AccountInfo newestInfo = accountRepository.findAccountInfoById(Long.valueOf(profileUpdateDTO.getAccountId()));
        updatedAccountInfo.setAccount(account);
        updatedAccountInfo.setPassword(newestInfo.getPassword());
        updatedAccountInfo.setFirstName(newestInfo.getFirstName());
        updatedAccountInfo.setLastName(newestInfo.getLastName());
        updatedAccountInfo.setSecondaryName(newestInfo.getSecondaryName());

        account.getAccountInfos().add(updatedAccountInfo);

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccountById(Long id) {
        if (id == null || !accountRepository.existsById(id)) {
            throw new IllegalArgumentException("ID nie istnieje lub nie może być null");
        }

        Account account = accountRepository.findById(id).isPresent() ? accountRepository.findById(id).get() : null;
        if (account == null) {
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

    @Override
    @Transactional(readOnly = true)
    public Account getAccountById(Integer id) {
        Optional<Account> account = accountRepository.findById(Long.valueOf(id));
        return account.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountInfo getNewestAccountInfoItemById(Integer id) {
        if (id == null) {
            return null;
        }

        return accountRepository.findAccountInfoById(id.longValue());
    }

    @Override
    @Transactional
    public void updatePassword(String email, String password) {
        if(email == null || password == null) {
            throw new IllegalArgumentException("Email or password null");
        }

        Account account = accountRepository.findAccountByEmail(email).orElse(null);

        if(account != null) {
            AccountInfo latestInfo = accountRepository.findAccountInfoById(account.getAccountId().longValue());
            if(latestInfo == null) {
                throw  new NullPointerException("Nie znaleziono account");
            }

            ProfileUpdateDTO profileUpdateDTO = new ProfileUpdateDTO(
                    account.getAccountId(),
                    latestInfo.getEmail(),
                    latestInfo.getPostalCodesTowns().getTown().getTownId(),
                    latestInfo.getPostalCodesTowns().getPostalCode().getPostalCode(),
                    latestInfo.getStreet(),
                    latestInfo.getStreetNumber(),
                    latestInfo.getApartmentNumber(),
                    latestInfo.getPhoneNumber(),
                    latestInfo.getPesel()
            );

            AccountInfo newInfo = accountFactory.createAccountInfo(profileUpdateDTO, latestInfo.getPostalCodesTowns());
            newInfo.setAccount(account);
            newInfo.setPassword(passwordEncoder.encode(password));
            newInfo.setFirstName(latestInfo.getFirstName());
            newInfo.setLastName(latestInfo.getLastName());
            newInfo.setSecondaryName(latestInfo.getSecondaryName());

            account.getAccountInfos().add(newInfo);
            accountRepository.save(account);
        }

    }
}
