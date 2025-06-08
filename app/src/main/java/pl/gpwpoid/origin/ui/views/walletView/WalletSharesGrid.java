package pl.gpwpoid.origin.ui.views.walletView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.services.WalletsService;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class WalletSharesGrid extends VerticalLayout {
    private final WalletsService walletsService;
    private final Grid<WalletCompanyListItem> sharesGrid = new Grid<>();
    private Integer walletId;

    public WalletSharesGrid(WalletsService walletsService) {
        this.walletsService = walletsService;
        add(sharesGrid);
        sharesGrid.setWidth("100%");
    }

    private void configureSharesGrid() {
        sharesGrid.addColumn(new ComponentRenderer<>(this::navigateToCompany))
                .setHeader("Firma")
                .setAutoWidth(true);

        sharesGrid.addColumn(WalletCompanyListItem::getCompanyCode)
                .setHeader("Kod")
                .setAutoWidth(true);

        sharesGrid.addColumn(item -> formatPrice(item.getCurrentSharePrice()))
                .setHeader("Aktualna cena")
                .setAutoWidth(true);

        sharesGrid.addColumn(this::formatPercentage)
                .setHeader("Zmiana")
                .setAutoWidth(true);

        sharesGrid.addColumn(WalletCompanyListItem::getSharesAmount)
                .setHeader("Ilość akcji")
                .setAutoWidth(true)
                .setSortable(true);

        sharesGrid.setWidth("900px");
    }

    public void setWallet(Integer walletId) {
        this.walletId = walletId;
        configureSharesGrid();
    }

    private Button navigateToCompany(WalletCompanyListItem item) {
        Button navigationButton = new Button(item.getCompanyName());
        navigationButton.addClickListener(e -> {
            String url = "/companies/" + item.getCompanyId();
            UI.getCurrent().navigate(url);
        });
        return navigationButton;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", price);
    }

    private String formatPercentage(WalletCompanyListItem item) {
        if (item.getPreviousSharePrice() == null)
            return "0%";
        BigDecimal percentage = item.getCurrentSharePrice().divide(item.getPreviousSharePrice(), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).add(new BigDecimal("-100"));
        return percentage + "%";
    }

    public void updateList() {
        sharesGrid.setItems(walletsService.getWalletCompanyListForCurrentWallet(walletId));
    }
}
