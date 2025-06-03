package pl.gpwpoid.origin.models.wallet;

import jakarta.persistence.*;
import pl.gpwpoid.origin.models.account.Account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Subscription;

import java.util.Set;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    @Column(name = "active")
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @OneToMany(mappedBy = "paymentWallet")
    private Set<IPO> iposAsPaymentWallet;

    @OneToMany(mappedBy = "wallet")
    private Set<pl.gpwpoid.origin.models.wallet.ExternalTransfer> externalTransfers;

    @OneToMany(mappedBy = "wallet")
    private Set<Order> orders;

    @OneToMany(mappedBy = "wallet")
    private Set<Subscription> subscriptions;
}
