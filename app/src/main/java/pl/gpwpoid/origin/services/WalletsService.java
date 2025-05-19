package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface WalletsService {
    void addWallet(Integer walletId,
                   Account account,
                   String walletName);

    Collection<Wallet> getWallets();
    Collection<WalletListItem> getWalletListViewForCurrentUser();
    Collection<WalletDTO> getWalletDTOForCurrentUser();
    Collection<Wallet> getWalletForCurrentUser();
    Optional<Wallet> getWalletById(Integer walletId);
    BigDecimal getWalletUnblockedFundsById(Integer walletId);
    Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId);
}
