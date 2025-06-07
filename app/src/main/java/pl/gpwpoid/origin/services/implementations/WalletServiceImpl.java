package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.factories.ExternalTransferFactory;
import pl.gpwpoid.origin.factories.WalletFactory;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.ExternalTransferRepository;
import pl.gpwpoid.origin.repositories.WalletRepository;
import pl.gpwpoid.origin.repositories.views.TransferListItem;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.TransferDTO;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletsService {
    private final WalletRepository walletRepository;
    private final WalletFactory walletFactory;
    private final AccountService accountService;
    private final ExternalTransferFactory externalTransferFactory;
    private final ExternalTransferRepository externalTransferRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository,
                             WalletFactory walletFactory,
                             AccountService accountService,
                             ExternalTransferFactory externalTransferFactory,
                             ExternalTransferRepository externalTransferRepository) {
        this.externalTransferFactory = externalTransferFactory;
        this.walletRepository = walletRepository;
        this.walletFactory = walletFactory;
        this.accountService = accountService;
        this.externalTransferRepository = externalTransferRepository;
    }

    @Override
    public void addWallet(WalletDTO walletDTO) {
        Account account = accountService.getAccountById(walletDTO.getAccountId());
        Wallet wallet = walletFactory.createWallet(account, walletDTO.getWalletName());
        walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<WalletListItem> getWalletListViewForCurrentUser() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        return walletRepository.getWalletListViewForCurrentUser(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Wallet> getWalletForCurrentUser() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        return walletRepository.getWalletForCurrentUser(accountId);
    }

    @Override
    public Optional<Wallet> getWalletById(Integer walletId) {
        return walletRepository.findById(Long.valueOf(walletId));
    }

    @Override
    public BigDecimal getWalletUnblockedFundsById(Integer walletId) {
        return walletRepository.getWalletUnblockedFundsById(walletId);
    }

    @Override
    public BigDecimal getWalletFundsById(Integer walletId) {
        return walletRepository.getFundsByWalletId(walletId);
    }

    @Override
    public String getWalletNameById(Integer walletId) {
        return walletRepository.getWalletNameById(walletId);
    }

    @Override
    public Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId) {
        return walletRepository.getWalletUnblockedSharesAmount(walletId, companyId);
    }

    @Override
    public Collection<WalletCompanyListItem> getWalletCompanyListForCurrentWallet(Integer walletId) {
        return walletRepository.getWalletCompanyListForCurrentWallet(walletId);
    }

    @Override
    public BigDecimal getWalletUnblockedFundsBeforeMarketBuyOrder(Integer orderId) {
        return walletRepository.getWalletUnblockedFundsBeforeMarketBuyOrder(orderId);
    }

    @Override
    public Collection<TransferListItem> getTransferListForCurrentWallet(Integer walletId) {
        return walletRepository.getTransferListForCurrentWallet(walletId);
    }

    @Override
    public void addTransfer(TransferDTO transferDTO) {
        Optional<Wallet> wallet = walletRepository.findById(Long.valueOf(transferDTO.getWalletId()));
        if (wallet.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }

        if (transferDTO.getTransferType().equals(ExternalTransfer.TransferType.withdrawal)) {
            BigDecimal funds = walletRepository.getFundsByWalletId(transferDTO.getWalletId());
            if (funds.compareTo(transferDTO.getFunds()) < 0)
                throw new IllegalArgumentException("Funds exceeded");
        }

        ExternalTransfer newTransfer = externalTransferFactory.createTransfer(
                transferDTO.getTransferType(),
                transferDTO.getFunds(),
                wallet.get(),
                transferDTO.getTransferDate(),
                transferDTO.getAccountNumber()
        );

        externalTransferRepository.save(newTransfer);
    }

    @Override
    public void deleteWallet(Integer walletId) {
        Optional<Wallet> wallet = walletRepository.findById(Long.valueOf(walletId));
        if (wallet.isPresent()) {
            Wallet walletToDelete = wallet.get();
            walletToDelete.setActive(false);
            walletRepository.save(walletToDelete);
        } else
            throw new IllegalArgumentException("Wallet not found");
    }
}
