package pl.gpwpoid.origin.ui.views;


import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;
import pl.gpwpoid.origin.repositories.views.TransactionWalletListItem;
import pl.gpwpoid.origin.repositories.views.TransferListItem;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.TransferDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;


@Route(value = "wallets", layout = MainLayout.class)
@RolesAllowed({"user", "admin"})
public class WalletView extends VerticalLayout implements HasUrlParameter<Integer> {
    private final TransactionService transactionService;
    WalletsService walletsService;

    private Integer walletId;
    private Collection<WalletCompanyListItem> walletCompanyListItems;
    private final Grid<WalletCompanyListItem> sharesGrid = new Grid<>();
    private final Grid<TransactionWalletListItem> transactionsGrid = new Grid<>();
    private final Grid<TransferListItem> transfersGrid = new Grid<>();
    private final H3 walletName = new H3();
    private Span walletFunds = new Span();
    private final Button deletionButton = new Button("Usuń portfel");
    private BigDecimal funds;

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    public WalletView(WalletsService walletsService, TransactionService transactionService) {
        this.walletsService = walletsService;
        this.transactionService = transactionService;

        if (SecurityUtils.isLoggedIn()) {
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            HorizontalLayout Layout1 = new HorizontalLayout(configureSharesGrid(), configureWalletStatusLayout());
            Layout1.setSizeFull();
            Layout1.setPadding(true);
            Layout1.setSpacing(true);
            HorizontalLayout Layout2 = new HorizontalLayout(configureTransfersGrid(), configureTransactionsGrid());
            Layout2.setSizeFull();
            Layout2.setPadding(true);
            Layout2.setSpacing(true);
            add(walletName, Layout1, Layout2);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.walletId = parameter;

        walletCompanyListItems = walletsService.getWalletCompanyListForCurrentWallet(walletId);
        sharesGrid.setItems(walletCompanyListItems);

        Collection<TransferListItem> transferListItems = walletsService.getTransferListForCurrentWallet(walletId);
        transfersGrid.setItems(transferListItems);

        Collection<TransactionWalletListItem> transactionWalletListItems = transactionService.getTransactionsByWalletId(walletId);
        transactionsGrid.setItems(transactionWalletListItems);

        funds = walletsService.getWalletFundsById(walletId);
        walletFunds.add(funds.toString() + " zł");
        String name = walletsService.getWalletNameById(walletId);
        walletName.add(name);

        deletionButton.setEnabled(canDeleteWallet());
    }

    private VerticalLayout configureSharesGrid() {
        VerticalLayout gridLayout = new VerticalLayout();

        sharesGrid.addColumn(new ComponentRenderer<>(item -> {
            Button navigationButton = new Button(item.getCompanyName());
            navigationButton.addClickListener(e -> {
                String url = "/companies/" + item.getCompanyId();
                UI.getCurrent().navigate(url);
            });
            return navigationButton;
        })).setHeader("Nazwa").setSortable(true).setAutoWidth(true);
        sharesGrid.addColumn(WalletCompanyListItem::getCompanyCode).setHeader("Kod").setSortable(true).setAutoWidth(true);
        sharesGrid.addColumn(WalletCompanyListItem::getCurrentSharePrice).setHeader("Aktualna cena").setSortable(true).setAutoWidth(true);
        sharesGrid.addColumn(item -> {
            if (item.getPreviousSharePrice() == null)
                return "0%";
            BigDecimal percentage = item.getCurrentSharePrice().divide(item.getPreviousSharePrice(), 10, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).add(new BigDecimal("-100"));
            return percentage + "%";
        }).setHeader("Zmiana").setSortable(true).setAutoWidth(true);
        sharesGrid.addColumn(WalletCompanyListItem::getSharesAmount).setHeader("Ilość akcji").setSortable(true).setAutoWidth(true);
        sharesGrid.setMaxWidth("900px");

        gridLayout.add(sharesGrid);
        return gridLayout;
    }

    private VerticalLayout configureTransactionsGrid() {
        VerticalLayout gridLayout = new VerticalLayout();

        transactionsGrid.addColumn(item -> {
            if (item.getOrderType().equals("sell"))
                return "sprzedaż";
            if (item.getOrderType().equals("buy"))
                return "Kupno";
            return null;
        }).setHeader("Typ").setSortable(true).setAutoWidth(true);
        transactionsGrid.addColumn(item -> FUNDS_FORMATTER.format(item.getAmount()) + " zł").setHeader("Kwota transakcji").setSortable(true).setAutoWidth(true);
        transactionsGrid.addColumn(item -> {
            if (item.getDate() == null)
                return null;
            LocalDateTime date = item.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return date.format(DATE_FORMATTER);
        }).setHeader("Data").setSortable(true).setAutoWidth(true);
        transactionsGrid.addColumn(TransactionWalletListItem::getSharesAmount).setHeader("Ilość akcji").setSortable(true).setAutoWidth(true);
        transactionsGrid.addColumn(new ComponentRenderer<>(item -> {
            Button navigationButton = new Button(item.getCompanyCode());
            navigationButton.addClickListener(e -> {
                String url = "/companies/" + item.getCompanyId();
                UI.getCurrent().navigate(url);
            });
            return navigationButton;
        })).setHeader("Firma").setSortable(true).setAutoWidth(true);
        transactionsGrid.setMaxWidth("700px");

        gridLayout.add(transactionsGrid);
        return gridLayout;
    }

    private VerticalLayout configureTransfersGrid() {
        VerticalLayout gridLayout = new VerticalLayout();

        transfersGrid.addColumn(item -> FUNDS_FORMATTER.format(item.getAmount()) + " zł").setHeader("Kwota").setSortable(true).setAutoWidth(true);
        transfersGrid.addColumn(item -> {
            if (item.getDate() == null)
                return null;
            LocalDateTime date = item.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return date.format(DATE_FORMATTER);
        }).setHeader("Data").setSortable(true).setAutoWidth(true);
        transfersGrid.addColumn(TransferListItem::getAccountNumber).setHeader("Konto").setSortable(true).setAutoWidth(true);

        gridLayout.add(transfersGrid);
        return gridLayout;
    }

    private VerticalLayout configureWalletStatusLayout() {
        VerticalLayout layout = new VerticalLayout();
        Button transferButton = new Button("Nowy transfer");
        transferButton.addClickListener(event -> openTransferDialog());

        deletionButton.setEnabled(false);
        deletionButton.addClickListener(event -> openDeletionDialog());

        walletFunds.add("Dostępne środki: ");
        layout.add(walletFunds, transferButton, deletionButton);
        layout.setMaxWidth("400px");
        return layout;
    }

    private boolean canDeleteWallet() {
        return walletCompanyListItems.isEmpty() && (funds.compareTo(new BigDecimal("0")) == 0);
    }

    private void openDeletionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Czy na pewno chcesz usunąć ten portfel?");
        Button confirmButton = new Button("Usuń");
        confirmButton.addClickListener(e -> {
            walletsService.deleteWallet(walletId);
            UI.getCurrent().navigate("/wallets");
            dialog.close();
        });
        Button cancelButton = new Button("Anuluj");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        dialog.getFooter().add(buttonLayout);

        dialog.open();
    }

    private void openTransferDialog() {
        TransferDTO transferDTO = new TransferDTO();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nowy transfer");
        dialog.setWidth("500px");

        TextField accountNumberField = new TextField("Numer konta");
        accountNumberField.setRequiredIndicatorVisible(true);
        accountNumberField.setWidth("100%");

        NumberField funds = new NumberField("Kwota");
        funds.setRequiredIndicatorVisible(true);
        funds.setWidth("100%");

        ComboBox<ExternalTransfer.TransferType> transferType = new ComboBox<>("Typ transferu");
        transferType.setRequiredIndicatorVisible(true);
        transferType.setItems(ExternalTransfer.TransferType.deposit, ExternalTransfer.TransferType.withdrawal);
        transferType.setItemLabelGenerator(
                type -> {
                    if (type.equals(ExternalTransfer.TransferType.withdrawal))
                        return "Wypłata";
                    if (type.equals(ExternalTransfer.TransferType.deposit))
                        return "Wpłata";
                    return null;
                }
        );
        transferType.setWidth("100%");


        Button saveTransferButton = new Button("Potwierdź", VaadinIcon.CHECK.create());
        saveTransferButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelTransaction = new Button("Anuluj");
        cancelTransaction.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(accountNumberField, funds, transferType);
        dialog.add(layout);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveTransferButton, cancelTransaction);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        dialog.getFooter().add(buttonLayout);

        Binder<TransferDTO> binder = new BeanValidationBinder<>(TransferDTO.class);
        binder.forField(accountNumberField).bind("accountNumber");
        binder.forField(funds)
                .withConverter(doubleValue -> {
                            if (doubleValue == null)
                                return null;
                            return BigDecimal.valueOf(doubleValue);
                        },
                        bigDecimal -> {
                            if (bigDecimal == null)
                                return null;
                            return bigDecimal.doubleValue();
                        },
                        "Nieprawidłowa kwota")
                .bind("funds");
        binder.forField(transferType).bind("transferType");
        binder.setBean(transferDTO);

        saveTransferButton.addClickListener(event -> {
            try {
                transferDTO.setWalletId(walletId);
                transferDTO.setTransferDate(new Date());
                binder.writeBean(transferDTO);
                walletsService.addTransfer(transferDTO);
                Notification.show("Wykonano przelew");
                updateFunds();
                dialog.close();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });

        cancelTransaction.addClickListener(event -> dialog.close());

        dialog.open();
    }

    private void updateFunds() {
        BigDecimal newFunds = walletsService.getWalletFundsById(walletId);
        funds = newFunds;
        walletFunds = new Span(newFunds.toString() + " zł");
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }
}