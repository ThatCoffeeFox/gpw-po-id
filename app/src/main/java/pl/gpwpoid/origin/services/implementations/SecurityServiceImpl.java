package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.repositories.AccountRepository;
import pl.gpwpoid.origin.repositories.views.AccountAuthItem;
import pl.gpwpoid.origin.utils.ExtendedUser;
import pl.gpwpoid.origin.utils.ExtendedUserDetails;

import java.util.Collection;
import java.util.Collections;

@Service
public class SecurityServiceImpl implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Autowired
    public SecurityServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public ExtendedUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        AccountAuthItem user = accountRepository.findAccountByEmailAsAuthItem(email);
        if (user == null) {
            throw new UsernameNotFoundException("Nie znaleziono użytkownika: " + email);
        }

        String roleName = "ROLE_" + user.getRole().name();
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleName));

        return new ExtendedUser(
                user.getEmail(),
                user.getPassword(),
                user.getAccountId(),
                true,
                true,
                true,
                true,
                authorities
        );
    }

}
