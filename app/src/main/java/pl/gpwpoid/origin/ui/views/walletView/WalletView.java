package pl.gpwpoid.origin.ui.views.walletView;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.MainLayout;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Objects;
import java.util.Optional;


@Route(value = "wallets", layout = MainLayout.class)
@RolesAllowed({"user", "admin"})
public class WalletView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final WalletsService walletsService;
    private final TransactionService transactionService;
    private final H3 walletName = new H3();
    private WalletSharesGrid walletSharesGrid;
    private WalletStatus walletStatus;
    private WalletTransactionsGrid walletTransactionsGrid;
    private WalletTransfersGrid walletTransfersGrid;
    private Integer walletId;

    @Autowired
    public WalletView(WalletsService walletsService, TransactionService transactionService) {
        this.walletsService = walletsService;
        this.transactionService = transactionService;

        if (SecurityUtils.isLoggedIn()) {
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            walletSharesGrid = new WalletSharesGrid(walletsService);
            walletStatus = new WalletStatus(walletsService);
            HorizontalLayout Layout1 = new HorizontalLayout();
            Layout1.add(walletSharesGrid, walletStatus);

            walletTransactionsGrid = new WalletTransactionsGrid(transactionService, walletsService);
            walletTransfersGrid = new WalletTransfersGrid(walletsService);
            add(walletName, Layout1, walletTransactionsGrid, walletTransfersGrid);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.walletId = parameter;

        if (SecurityUtils.isLoggedIn()) {
            Integer accountId = SecurityUtils.getAuthenticatedAccountId();
            Optional<Wallet> wallet = walletsService.getWalletById(walletId);
            if (wallet.isEmpty() || !Objects.equals(wallet.get().getAccount().getAccountId(), accountId)) {
                beforeEvent.rerouteToError(IllegalAccessException.class, "Nie masz dostępu do tego portfela.");
            } else {
                walletSharesGrid.setWallet(walletId);
                walletStatus.setWallet(walletId);
                walletTransactionsGrid.setWallet(walletId);
                walletTransfersGrid.setWallet(walletId);

                walletSharesGrid.updateList();
                walletTransactionsGrid.updateList();
                walletTransfersGrid.updateList();
                String name = walletsService.getWalletNameById(walletId);
                walletName.add(name);
            }
        }
    }
}