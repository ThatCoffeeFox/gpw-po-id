package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.account.Account;
import pl.gpwpoid.origin.models.factories.WalletFactory;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.WalletRepository;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletsService {
    private final WalletRepository walletRepository;
    private final WalletFactory walletFactory;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository, WalletFactory walletFactory) {
        this.walletRepository = walletRepository;
        this.walletFactory = walletFactory;
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
    public Collection<WalletListItem> getWalletListViewForCurrentUser() {
        String email = SecurityUtils.getAuthenticatedEmail();
        return walletRepository.getWalletListViewForCurrentUser(email);
    }

    @Override
    public Collection<WalletDTO> getWalletDTOForCurrentUser() {
        String email = SecurityUtils.getAuthenticatedEmail();
        List<Wallet> wallets = walletRepository.getWalletForCurrentUser(email);
        return wallets.stream().map(wallet -> new WalletDTO(wallet.getWalletId(), wallet.getName())).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Wallet> getWalletForCurrentUser(){
        String email = SecurityUtils.getAuthenticatedEmail();
        return walletRepository.getWalletForCurrentUser(email);
    }

    @Override
    public Optional<Wallet> getWalletById(Integer walletId) {
        return walletRepository.findById(Long.valueOf(walletId));
    }
}
