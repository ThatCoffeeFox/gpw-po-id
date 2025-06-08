package pl.gpwpoid.origin.ui.views.walletView;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.gpwpoid.origin.repositories.views.TransferListItem;
import pl.gpwpoid.origin.services.WalletsService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class WalletTransfersGrid extends VerticalLayout {
    private final WalletsService walletsService;

    private final Grid<TransferListItem> transfersGrid = new Grid<>();

    private Integer walletId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public WalletTransfersGrid(WalletsService walletsService) {
        this.walletsService = walletsService;
        add(transfersGrid);
    }

    private void configureTransfersGrid() {
        transfersGrid.addColumn(this::formatPrice)
                .setHeader("Kwota")
                .setAutoWidth(true);

        transfersGrid.addColumn(this::formatDate)
                .setHeader("Data")
                .setAutoWidth(true);

        transfersGrid.addColumn(TransferListItem::getAccountNumber)
                .setHeader("Konto")
                .setAutoWidth(true);

        setWidth("100%");
    }

    public void setWallet(Integer walletId) {
        this.walletId = walletId;
        configureTransfersGrid();
    }

    public void updateList(){
        Collection<TransferListItem> transferListItems = walletsService.getTransferListForCurrentWallet(walletId);
        transfersGrid.setItems(transferListItems);
    }

    private String formatPrice(TransferListItem item) {
        if (item == null || item.getAmount() == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", item.getAmount());
    }

    private String formatDate(TransferListItem item) {
        if (item.getDate() == null)
            return null;
        LocalDateTime date = item.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date.format(DATE_FORMATTER);
    }
}
