package pl.gpwpoid.origin.ui.views.walletView;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.TransferListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.TransferDTO;

import java.math.BigDecimal;
import java.util.Date;

public class WalletStatus extends VerticalLayout {
    private final WalletsService walletsService;
    private Integer walletId;

    private final Button transferButton = new Button("Nowy Transfer");
    private final Button deletionButton = new Button("Usuń portfel");
    private final TextField walletFunds = new TextField("Dostępne środki:");
    private BigDecimal funds;

    private final TextField accountNumberField = new TextField("Numer konta");
    private final NumberField transferFunds = new NumberField("Kwota");
    private final ComboBox<ExternalTransfer.TransferType> transferType = new ComboBox<>("Typ transferu");

    private TransferDTO transferDTO = new TransferDTO();
    private final Binder<TransferDTO> binder = new BeanValidationBinder<>(TransferDTO.class);

    public WalletStatus(WalletsService walletsService) {
        this.walletsService = walletsService;

        add(walletFunds, transferButton, deletionButton);
        configureWalletStatusLayout();
    }

    private void configureWalletStatusLayout() {
        transferButton.addClickListener(event -> openTransferDialog());
        deletionButton.addClickListener(event -> openDeletionDialog());
        walletFunds.setReadOnly(true);
    }

    private void openDeletionDialog() {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("Czy na pewno chcesz usunąć ten portfel?");

        Button confirmButton = new Button("Usuń");
        Button cancelButton = new Button("Anuluj");

        confirmButton.addClickListener(e -> {
            walletsService.deleteWallet(walletId);
            UI.getCurrent().navigate("/wallets");
            dialog.close();
        });
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        dialog.getFooter().add(buttonLayout);

        dialog.open();
    }

    public void setWallet(Integer walletId) {
        this.walletId = walletId;
        updateFunds();
        deletionButton.setEnabled(canDeleteWallet());
        configureTransferFields();
        bindTransferFields();
    }

    private boolean canDeleteWallet() {
        return walletsService.getWalletCompanyListForCurrentWallet(walletId).isEmpty() && funds.compareTo(BigDecimal.ZERO) == 0;
    }

    private void openTransferDialog() {
        TransferDTO transferDTO = new TransferDTO();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nowy transfer");
        dialog.setWidth("500px");

        Button saveTransferButton = new Button("Potwierdź", VaadinIcon.CHECK.create());
        saveTransferButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelTransaction = new Button("Anuluj");
        cancelTransaction.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        VerticalLayout layout = new VerticalLayout(accountNumberField, transferFunds, transferType);
        dialog.add(layout);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveTransferButton, cancelTransaction);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        dialog.getFooter().add(buttonLayout);

        saveTransferButton.addClickListener(event -> confirmTransferButton(dialog));
        cancelTransaction.addClickListener(event -> dialog.close());

        dialog.open();
    }

    private void configureTransferFields(){
        accountNumberField.setRequiredIndicatorVisible(true);
        accountNumberField.setWidth("100%");

        transferFunds.setRequiredIndicatorVisible(true);
        transferFunds.setWidth("100%");

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
    }

    private void bindTransferFields() {
        Binder<TransferDTO> binder = new BeanValidationBinder<>(TransferDTO.class);
        binder.forField(accountNumberField).bind("accountNumber");
        binder.forField(transferFunds)
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
    }

    private void confirmTransferButton(Dialog dialog){
        try {
            transferDTO.setWalletId(walletId);
            transferDTO.setTransferDate(new Date());
            binder.writeBean(transferDTO);
            walletsService.addTransfer(transferDTO);
            Notification.show("Wykonano przelew");
            updateFunds();
            dialog.close();
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }

    private void updateFunds() {
        funds = walletsService.getWalletFundsById(walletId);
        walletFunds.setValue(formatPrice(funds));
    }

    private String formatPrice(BigDecimal item) {
        if (item == null) {
            return "Brak danych";
        }
        return String.format("%.2f PLN", item);
    }
}
