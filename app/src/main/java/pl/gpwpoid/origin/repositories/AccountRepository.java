package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.repositories.views.AccountAuthItem;
import pl.gpwpoid.origin.repositories.views.AccountListItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("""
        SELECT new pl.gpwpoid.origin.repositories.views.AccountListItem(
                a.accountId,
                ai.firstName,
                ai.secondaryName,
                ai.lastName,
                ai.email,
                ai.phoneNumber,
                ai.pesel,
                ai.postalCodesTowns.town.name,
                ai.postalCodesTowns.postalCode.postalCode,
                ai.street,
                ai.streetNumber,
                ai.apartmentNumber
        )
        FROM Account a LEFT JOIN AccountInfo ai ON a.accountId = ai.id.accountId
        WHERE ai.id.updatedAt = (
            SELECT MAX(ai_inner.id.updatedAt)
            FROM AccountInfo ai_inner
            WHERE ai_inner.account = a
        )
    """)
    List<AccountListItem> findAllAccountsAsViewItems();

    @Query("""
        SELECT new pl.gpwpoid.origin.repositories.views.AccountListItem(
                a.accountId,
                ai.firstName,
                ai.secondaryName,
                ai.lastName,
                ai.email,
                ai.phoneNumber,
                ai.pesel,
                ai.postalCodesTowns.town.name,
                ai.postalCodesTowns.postalCode.postalCode,
                ai.street,
                ai.streetNumber,
                ai.apartmentNumber
        )
        FROM Account a LEFT JOIN AccountInfo ai ON a.accountId = ai.id.accountId
        WHERE ai.id.updatedAt = (
            SELECT MAX(ai_inner.id.updatedAt)
            FROM AccountInfo ai_inner
            WHERE ai_inner.account = a
        ) AND a.accountId = :id
    """)
    Optional<AccountListItem> findAccountByIdAsViewItem(Long id);

    @Query("""
        SELECT new pl.gpwpoid.origin.repositories.views.AccountAuthItem(
            a.accountId,
            ai.email,
            ai.password,
            a.role
        )
        FROM Account a
        JOIN AccountInfo ai ON ai.account = a
        WHERE ai.email = :email
        AND ai.id.updatedAt = (
            SELECT MAX(ai_inner.id.updatedAt)
            FROM AccountInfo ai_inner
            WHERE ai_inner.account = a
        )
    """)
    AccountAuthItem findAccountByEmailAsAuthItem(String email);

    @Query("""
        SELECT a
        FROM Account a
        JOIN AccountInfo ai ON ai.account = a
        WHERE ai.email = :email
        AND ai.id.updatedAt = (
            SELECT MAX(ai_inner.id.updatedAt)
            FROM AccountInfo ai_inner
            WHERE ai_inner.account = a
        )
    """)
    Optional<Account> findAccountByEmail(String email);
}

