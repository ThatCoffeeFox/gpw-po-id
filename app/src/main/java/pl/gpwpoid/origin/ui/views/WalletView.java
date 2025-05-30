package pl.gpwpoid.origin.ui.views;


import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Collection;

//TODO: dodac ile pieniedzy sie wydalo na akcje danej firmy

@Route("wallets")
@RolesAllowed({"user", "admin"})
public class WalletView extends HorizontalLayout implements HasUrlParameter<Integer> {
    WalletsService walletsService;

    private Integer walletId;
    private final Grid<WalletCompanyListItem> grid = new Grid<>();

    @Autowired
    public WalletView(WalletsService walletsService) {
        this.walletsService = walletsService;

        if(SecurityUtils.isLoggedIn()) {
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            VerticalLayout gridLayout = configureGrid();
            add(gridLayout);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.walletId = parameter;
        Collection<WalletCompanyListItem> walletCompanyListItems = walletsService.getWalletCompanyListForCurrentWallet(walletId);
        grid.setItems(walletCompanyListItems);
    }

    private VerticalLayout configureGrid() {
        VerticalLayout gridLayout = new VerticalLayout();

        grid.addColumn(WalletCompanyListItem::getCompanyName).setHeader("Nazwa").setSortable(true);
        grid.addColumn(WalletCompanyListItem::getCompanyCode).setHeader("Kod").setSortable(true);
        grid.addColumn(WalletCompanyListItem::getSharePrice).setHeader("Aktualna cena").setSortable(true); //TODO: aktualizacja w czasie rzeczywistym
        grid.addColumn(WalletCompanyListItem::getSharesAmount).setHeader("Ilość akcji").setSortable(true);

        gridLayout.add(grid);
        return gridLayout;
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }
}
