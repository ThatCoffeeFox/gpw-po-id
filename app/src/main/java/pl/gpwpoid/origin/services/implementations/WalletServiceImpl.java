package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.factories.WalletFactory;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.WalletRepository;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletsService {
    private final WalletRepository walletRepository;
    private final WalletFactory walletFactory;
    private final AccountService accountService;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, WalletFactory walletFactory, AccountService accountService) {
        this.walletRepository = walletRepository;
        this.walletFactory = walletFactory;
        this.accountService = accountService;
    }

    @Override
    public void addWallet(WalletDTO walletDTO) {
        Account account = accountService.getAccountById(walletDTO.getAccountId());
        Wallet wallet = walletFactory.createWallet(account, walletDTO.getWalletName());
        walletRepository.save(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Wallet> getWallets() {
        return walletRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<WalletListItem> getWalletListViewForCurrentUser() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        return walletRepository.getWalletListViewForCurrentUser(accountId);
    }

    @Override
    public Collection<WalletDTO> getWalletDTOForCurrentUser() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        List<Wallet> wallets = walletRepository.getWalletForCurrentUser(accountId);
        return wallets.stream().map(wallet -> new WalletDTO(wallet.getWalletId(), wallet.getName())).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Wallet> getWalletForCurrentUser(){
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
    public Integer getWalletUnblockedSharesAmount(Integer walletId, Integer companyId) {
        return walletRepository.getWalletUnblockedSharesAmount(walletId, companyId);
    }

    @Override
    public Collection<WalletCompanyListItem> getWalletCompanyListForCurrentWallet(Integer walletId) {
        return walletRepository.getWalletCompanyListForCurrentWallet(walletId);
    }
}
