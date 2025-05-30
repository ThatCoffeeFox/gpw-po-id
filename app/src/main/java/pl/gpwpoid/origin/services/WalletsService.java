package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.ui.views.DTO.TransferDTO;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface WalletsService {
    void addWallet(WalletDTO walletDTO);
    Collection<WalletListItem> getWalletListViewForCurrentUser();
    Collection<Wallet> getWalletForCurrentUser();
    Optional<Wallet> getWalletById(Integer walletId);
    BigDecimal getWalletUnblockedFundsById(Integer walletId);
    BigDecimal getWalletFundsById(Integer walletId);
    String getWalletNameById(Integer walletId);
    Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId);
    Collection<WalletCompanyListItem> getWalletCompanyListForCurrentWallet(Integer walletId);
    void addTransfer(TransferDTO transferDTO);
}
