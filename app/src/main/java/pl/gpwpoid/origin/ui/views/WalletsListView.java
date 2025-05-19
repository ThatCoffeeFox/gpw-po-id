package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

@Route("wallets")
@PageTitle("Lista portfeli")
@PermitAll
public class WalletsListView extends VerticalLayout {
    private final WalletsService walletsService;
    private final Grid<WalletListItem> grid = new Grid<>();

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    public WalletsListView(WalletsService walletsService) {
        this.walletsService = walletsService;

        setSizeFull();

        configureGrid();
        add(grid);
        loadWalletListItems();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(WalletListItem::getName).setHeader("Nazwa portfela").setSortable(true);

        grid.addColumn(
                wallet -> {
                    return FUNDS_FORMATTER.format(wallet.getFunds()) + " zł";
                })
                .setHeader("Dostępne fundusze")
                .setTextAlign(ColumnTextAlign.END).setSortable(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addItemClickListener(event -> {
            WalletListItem item = event.getItem();
            if (item != null && item.getFunds() != null) {
                UI.getCurrent().navigate("wallets/" + item.getWalletId());
            }
        });

        grid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private void loadWalletListItems() {
        if(SecurityUtils.isLoggedIn()) {
            Collection<WalletListItem> walletList = walletsService.getWalletListViewForCurrentUser();
            grid.setItems(walletList);
        }
        else
            grid.setItems(Collections.emptyList());
    }
}
