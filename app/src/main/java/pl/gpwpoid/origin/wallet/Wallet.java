package pl.gpwpoid.origin.wallet;


import jakarta.persistence.*;
import pl.gpwpoid.origin.account.Account;

import java.util.Set;

@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "wallet_id")
    private Long id;

    @MapsId("accountId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "wallet", fetch = FetchType.EAGER)
    private Set<Transfer> transfers;

    @Column(name = "name", nullable = false)
    private String name;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Transfer> getTransfers() {
        return transfers;
    }

}
