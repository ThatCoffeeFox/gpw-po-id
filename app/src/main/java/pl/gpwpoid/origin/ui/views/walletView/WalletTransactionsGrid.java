package pl.gpwpoid.origin.ui.views.walletView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class WalletTransactionsGrid extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TransactionService transactionService;
    private final WalletsService walletsService;
    private final Grid<TransactionWalletListItem> transactionsGrid = new Grid<>();
    private Integer walletId;

    public WalletTransactionsGrid(TransactionService transactionService, WalletsService walletsService) {
        this.transactionService = transactionService;
        this.walletsService = walletsService;

        add(transactionsGrid);
    }

    private void configureTransactionsGrid() {

        transactionsGrid.addColumn(this::translateOrderType)
                .setHeader("Typ")
                .setAutoWidth(true);

        transactionsGrid.addColumn(this::formatPrice)
                .setHeader("Kwota transakcji")
                .setAutoWidth(true);

        transactionsGrid.addColumn(this::formatDate)
                .setHeader("Data")
                .setSortable(true)
                .setAutoWidth(true);

        transactionsGrid.addColumn(TransactionWalletListItem::getSharesAmount)
                .setHeader("Ilość akcji")
                .setAutoWidth(true);

        transactionsGrid.addColumn(new ComponentRenderer<>(this::navigateToCompany))
                .setHeader("Firma")
                .setAutoWidth(true);

        transactionsGrid.setMaxWidth("100%");
    }

    public void updateList() {
        Collection<TransactionWalletListItem> transactionWalletListItems = transactionService.getTransactionsByWalletId(walletId);
        transactionsGrid.setItems(transactionWalletListItems);
    }

    public void setWallet(Integer walletId) {
        this.walletId = walletId;
        configureTransactionsGrid();
    }

    private String formatPrice(TransactionWalletListItem item) {
        if (item == null || item.getAmount() == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", item.getAmount());
    }

    private String formatDate(TransactionWalletListItem item) {
        if (item.getDate() == null)
            return null;
        LocalDateTime date = item.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date.format(DATE_FORMATTER);
    }

    private Button navigateToCompany(TransactionWalletListItem item) {
        Button navigationButton = new Button(item.getCompanyCode());
        navigationButton.addClickListener(e -> {
            String url = "/companies/" + item.getCompanyId();
            UI.getCurrent().navigate(url);
        });
        return navigationButton;
    }

    private String translateOrderType(TransactionWalletListItem item) {
        if (item.getOrderType().equals("sell"))
            return "sprzedaż";
        if (item.getOrderType().equals("buy"))
            return "Kupno";
        return null;
    }
}
