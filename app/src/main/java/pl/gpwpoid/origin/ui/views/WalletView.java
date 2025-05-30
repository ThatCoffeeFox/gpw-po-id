package pl.gpwpoid.origin.ui.views;


import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.wallet.ExternalTransfer;
import pl.gpwpoid.origin.repositories.views.WalletCompanyListItem;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.TransferDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;


@Route(value = "wallets", layout = MainLayout.class)
@RolesAllowed({"user", "admin"})
public class WalletView extends HorizontalLayout implements HasUrlParameter<Integer> {
    WalletsService walletsService;

    private Integer walletId;
    private final Grid<WalletCompanyListItem> grid = new Grid<>();
    private final Span walletFunds = new Span();

    @Autowired
    public WalletView(WalletsService walletsService) {
        this.walletsService = walletsService;

        if(SecurityUtils.isLoggedIn()) {
            setSizeFull();
            setPadding(true);
            setSpacing(true);

            VerticalLayout gridLayout = configureGrid();
            VerticalLayout transferLayout = configureWalletStatusLayout();
            add(gridLayout, transferLayout);
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
        grid.setMaxWidth("700px");

        gridLayout.add(grid);
        return gridLayout;
    }

    private VerticalLayout configureWalletStatusLayout(){
        VerticalLayout layout = new VerticalLayout();
        Button tranferButton = new Button("Nowy transfer");
        tranferButton.addClickListener(event -> openTransferDialog());

        layout.add(walletFunds, tranferButton);
        return layout;
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
                type ->{
                    if(type.equals(ExternalTransfer.TransferType.withdrawal))
                        return "Wypłata";
                    if(type.equals(ExternalTransfer.TransferType.deposit))
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
                    if(bigDecimal == null)
                        return null;
                    return bigDecimal.doubleValue();
                },
                        "Nieprawidłowa kwota")
                .bind("funds");
        binder.forField(transferType).bind("transferType");
        binder.setBean(transferDTO);

        saveTransferButton.addClickListener(event -> {
            try{
                transferDTO.setWalletId(walletId);
                transferDTO.setTransferDate(new Date());
                binder.writeBean(transferDTO);
                walletsService.addTransfer(transferDTO);
                Notification.show("Wykonano przelew");
                dialog.close();
            } catch (Exception e){
                    showError(e.getMessage());
            }
        });

        cancelTransaction.addClickListener(event -> dialog.close());

        dialog.open();
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }
}
