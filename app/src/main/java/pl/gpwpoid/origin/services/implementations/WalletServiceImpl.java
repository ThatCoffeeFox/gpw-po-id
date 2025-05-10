package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.factories.WalletFactory;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.WalletRepository;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Collection;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletsService {
    private final WalletRepository walletRepository;
    private final WalletFactory walletFactory;
    private final SecurityServiceImpl securityService;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, WalletFactory walletFactory, SecurityServiceImpl securityService) {
        this.walletRepository = walletRepository;
        this.walletFactory = walletFactory;
        this.securityService = securityService;
    }

    @Override
    public void addWallet(Integer walletId, Account account, String walletName) {
        Wallet newWallet = walletFactory.createWallet(walletId, account, walletName);
        walletRepository.save(newWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Wallet> getWallets() {
        return walletRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<WalletListItem> getWalletsForCurrentUser() {
        String email = SecurityUtils.getAuthenticatedEmail();
        return walletRepository.getWalletsForCurrentUser(email);
    }
}
