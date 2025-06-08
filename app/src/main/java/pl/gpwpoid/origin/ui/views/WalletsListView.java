package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.repositories.views.WalletListViewItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;

@Route(value = "wallets", layout = MainLayout.class)
@PageTitle("Lista portfeli")
@RolesAllowed({"user", "admin"})
public class WalletsListView extends VerticalLayout {
    private final WalletsService walletsService;
    private final Grid<WalletListViewItem> grid = new Grid<>();

    public WalletsListView(WalletsService walletsService) {
        this.walletsService = walletsService;

        setSizeFull();

        configureGrid();
        VerticalLayout addWalletLayout = configureAddWalletButton();
        add(addWalletLayout, new H3("Twoje portfele: "), grid);
        loadWalletListItems();
        setSpacing(true);
        setPadding(true);
    }

    private VerticalLayout configureAddWalletButton() {
        Button addWalletButton = new Button("Dodaj portfel", VaadinIcon.PLUS.create());
        addWalletButton.addClickListener(e -> openAddWalletDialog());

        VerticalLayout addWalletLayout = new VerticalLayout();
        addWalletLayout.add(addWalletButton);
        addWalletLayout.setWidth("100px");
        return addWalletLayout;
    }

    private void openAddWalletDialog() {
        WalletDTO walletDTO = new WalletDTO();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nowy portfel");

        TextField walletName = new TextField("Nazwa portfela");
        walletName.setRequiredIndicatorVisible(true);
        walletName.setErrorMessage("Nazwa nie może być pusta");

        Button saveWalletButton = new Button("Zapisz", VaadinIcon.CHECK.create());
        saveWalletButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout addWalletLayout = new VerticalLayout(walletName);
        dialog.add(addWalletLayout);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveWalletButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        dialog.getFooter().add(buttonLayout);

        Binder<WalletDTO> binder = new BeanValidationBinder<>(WalletDTO.class);
        binder.forField(walletName).bind("walletName");
        binder.setBean(walletDTO);

        saveWalletButton.addClickListener(e -> {
            try {
                if (SecurityUtils.isLoggedIn())
                    walletDTO.setAccountId(SecurityUtils.getAuthenticatedAccountId());
                binder.writeBean(walletDTO);
                walletsService.addWallet(walletDTO);
                loadWalletListItems();
                dialog.close();
            } catch (ValidationException ex) {
                Notification.show(ex.getMessage());
            }
        });

        cancelButton.addClickListener(e -> dialog.close());

        dialog.open();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(WalletListViewItem::getWalletName)
                .setHeader("Nazwa portfela")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(item -> formatPrice(item.getWalletFunds()))
                .setHeader("Dostępne fundusze")
                .setAutoWidth(true);

        grid.addColumn(WalletListViewItem::getWalletShares)
                .setHeader("Ilość Wszystkich akcji")
                .setAutoWidth(true);

        grid.addColumn(item -> formatPrice(item.getWalletSharesValue()))
                .setHeader("Łączna wartość akcji")
                .setAutoWidth(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addItemClickListener(event -> {
            WalletListViewItem item = event.getItem();
            if (item != null && item.getWalletId() != null) {
                UI.getCurrent().navigate("wallets/" + item.getWalletId());
            }
        });

        grid.setMaxWidth("100%");
    }

    private void loadWalletListItems() {
        if (SecurityUtils.isLoggedIn()) {
            Collection<WalletListViewItem> walletList = walletsService.getExtendedWalletListViewByAccountId(SecurityUtils.getAuthenticatedAccountId());
            grid.setItems(walletList);
        } else
            grid.setItems(Collections.emptyList());
    }

    private String formatPrice(BigDecimal item) {
        if (item == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", item);
    }
}