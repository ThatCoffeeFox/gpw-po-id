package pl.gpwpoid.origin.models.account;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.wallet.Wallet;

import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
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
    @Column(name = "role", nullable = false, columnDefinition = "user_role") // user_role to typ ENUM w DB
    private UserRole role;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountInfo> accountInfos;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true) // ON DELETE CASCADE w SQL
    private Set<Wallet> wallets;
}