package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletListItem;

import java.util.Collection;

public interface WalletsService {
    void addWallet(Integer walletId,
                   Account account,
                   String walletName);

    Collection<Wallet> getWallets();
    Collection<WalletListItem> getWalletsForCurrentUser();
}
