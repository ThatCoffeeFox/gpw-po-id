package pl.gpwpoid.origin.models.account;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    public enum UserRole {
        admin,
        user
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountInfo> accountInfos;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wallet> wallets;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        if (accountId == null) {
            return false;
        }
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId=" + accountId +
                ", role=" + role +
                '}';
    }
}