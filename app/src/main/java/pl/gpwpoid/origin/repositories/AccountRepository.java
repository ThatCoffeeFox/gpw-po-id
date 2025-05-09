package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.repositories.views.AccountListItem;

import java.util.List;

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
                SELECT MAX(ai2.id.updatedAt)
                FROM AccountInfo ai2
                WHERE ai2.id.accountId = a.accountId
            )
    """)
    List<AccountListItem> findAllAccountsAsViewItems();
}

