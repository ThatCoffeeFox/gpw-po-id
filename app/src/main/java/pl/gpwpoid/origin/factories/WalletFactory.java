package pl.gpwpoid.origin.models.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.wallet.Wallet;

@Component
public class WalletFactory {
    public Wallet createWallet(
            Integer walletId,
            Account account,
            String name
    ) {
        if(walletId == null)
            throw new IllegalArgumentException("walletId cannot be null");
        if(account == null)
            throw new IllegalArgumentException("account cannot be null");
        if(name == null)
            throw new IllegalArgumentException("name cannot be null");

        Wallet wallet = new Wallet();
        wallet.setWalletId(walletId);
        wallet.setAccount(account);
        wallet.setName(name);

        return wallet;
    }
}
