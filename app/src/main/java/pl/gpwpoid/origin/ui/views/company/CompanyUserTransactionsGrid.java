package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.data.domain.PageRequest;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.WalletView;
import pl.gpwpoid.origin.utils.ExtendedUserDetails;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class CompanyUserTransactionsGrid extends VerticalLayout {

    private final TransactionService transactionService;
    private final WalletsService walletsService;
    private final Grid<TransactionWalletListItem> grid = new Grid<>();

    private Integer companyId;
    private final Integer userId;

    public CompanyUserTransactionsGrid(TransactionService transactionService,  WalletsService walletsService) {
        this.transactionService = transactionService;
        this.walletsService = walletsService;
        this.userId = SecurityUtils.getAuthenticatedUser().map(ExtendedUserDetails::getAccountId).orElse(null);

        H4 header = new H4("Moje ostatnie transakcje w tej spółce");

        configureGrid();

        add(header, grid);
        setWidthFull();
    }

    private void configureGrid() {
        grid.addColumn(item -> Objects.equals(item.getOrderType(), "sell") ? "Sprzedaż" : "Kupno").setHeader("Typ");
        grid.addColumn(item -> item.getDate()).setHeader("Data");
        grid.addColumn(item -> item.getAmount() + " zł").setHeader("Wartość");
        grid.addColumn(TransactionWalletListItem::getSharesAmount).setHeader("Liczba akcji");
        grid.addComponentColumn(item -> new Button(walletsService.getWalletNameById(item.getWalletId()),
                e -> {
                    UI.getCurrent().navigate("/wallets/" + item.getWalletId());
        })).setHeader("Portfel");
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        refresh();
    }

    public void refresh() {
        if (companyId == null || userId == null) {
            grid.setItems(Collections.emptyList());
            return;
        }

        grid.setItems(transactionService.getTransactionsByCompanyAndUser(
                companyId,
                userId,
                PageRequest.of(0, 10)
        ));
    }
}