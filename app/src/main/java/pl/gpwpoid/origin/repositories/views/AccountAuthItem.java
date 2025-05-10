package pl.gpwpoid.origin.repositories.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.account.Account;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountAuthItem {
    private Integer accountId;
    private String email;
    private String password;
    private Account.UserRole role;
}
