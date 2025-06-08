package pl.gpwpoid.origin.ui.views.AdminCompanyView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.AccountService;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.IPODTO;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class AdminStartIPODialog extends VerticalLayout {
    private final IPOService ipoService;
    private final AccountService accountService;
    private final WalletsService walletService;
    private final CompanyService companyService;

    private Integer companyId;

    private final ComboBox<AccountListItem> ownerAccount = new ComboBox<>("Właściciel Portfela");
    private final ComboBox<WalletListItem> ownerWallet = new ComboBox<>("Portfel");
    private final IntegerField sharesAmount = new IntegerField("Ilość akcji");
    private final NumberField sharePrice = new NumberField("Cena akcji");
    private final DateTimePicker subscriptionEnd = new DateTimePicker("Data zakończenia");
    private final Binder<IPODTO> binder = new BeanValidationBinder<>(IPODTO.class);

    private final Button confirmButton = new Button("Zatwierdź");
    private final Button cancelButton = new Button("Anuluj");
    private final Dialog dialog = new Dialog();

    private final Button startIPOButton = new Button("Rozpocznij emisję");

    private IPODTO ipoDTO = new IPODTO();

    public AdminStartIPODialog(IPOService ipoService, AccountService accountService, WalletsService walletService, CompanyService companyService) {
        this.ipoService = ipoService;
        this.accountService = accountService;
        this.walletService = walletService;
        this.companyService = companyService;


        add(startIPOButton);
        setWidth("200px");

        startIPOButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startIPOButton.setEnabled(false);
        startIPOButton.addClickListener(e -> openDialog());

        confirmButton.addClickListener(e -> confirmNewIPO());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton.addClickListener(e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        configureDialog();
        configureFields();
        bindFields();
    }

    private void openDialog() {
        Collection<AccountListItem> accounts = accountService.getAccountViewList();
        ownerAccount.setItems(accounts);
        dialog.open();
    }

    private void configureDialog(){
        dialog.setHeaderTitle("Nowe IPO");
        dialog.setWidth("500px");

        HorizontalLayout layout1 = new HorizontalLayout(ownerAccount, ownerWallet);
        HorizontalLayout layout2 = new HorizontalLayout(sharesAmount, sharePrice);
        HorizontalLayout layout3 = new HorizontalLayout(subscriptionEnd);
        VerticalLayout layout = new VerticalLayout(layout1, layout2, layout3);
        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        dialog.add(layout);
        dialog.getFooter().add(buttonLayout);
    }

    private void configureFields(){
        ownerAccount.setItemLabelGenerator(item ->
                String.format("%s %s %s (%s)",
                        item.getFirstName(),
                        item.getSecondaryName() != null ? item.getSecondaryName() : "",
                        item.getLastName(),
                        item.getPesel()
                ).replaceAll("\\s+", " ")
        );
        ownerAccount.setRequiredIndicatorVisible(true);
        ownerAccount.addValueChangeListener(e -> {
            AccountListItem accountListItem = (AccountListItem) e.getValue();

            if(accountListItem != null){
                Integer accountId = accountListItem.getAccountId();

                Collection<WalletListItem> ownerWallets = walletService.getWalletListViewByAccountId(accountId);
                ownerWallet.setItems(ownerWallets);

                ownerWallet.setEnabled(true);
                ownerWallet.setPlaceholder("Wybierz portfel");
            }
            else {
                ownerWallet.clear();
                ownerWallet.setItems(Collections.emptyList());
                ownerWallet.setEnabled(false);
                ownerWallet.setPlaceholder("Najpierw wybierz użytkownika");
            }

            if(binder.getBean() != null)
                binder.getBean().setWalletOwnerId(null);
        });

        ownerWallet.setItemLabelGenerator(WalletListItem::getName);
        ownerWallet.setRequiredIndicatorVisible(true);
        ownerWallet.setEnabled(false);
        ownerWallet.setPlaceholder("Najpierw wybierz użytkownika");

        sharesAmount.setRequiredIndicatorVisible(true);
        sharePrice.setRequiredIndicatorVisible(true);

        subscriptionEnd.setLocale(new Locale("pl", "PL"));
        subscriptionEnd.setStep(Duration.ofMinutes(1));
        subscriptionEnd.setRequiredIndicatorVisible(true);
    }

    private void bindFields(){
        binder.forField(ownerWallet)
                        .withConverter(
                                walletListItem -> (walletListItem == null) ? null : walletListItem.getWalletId(),
                                walletId -> {
                                    if(walletId == null)
                                        return null;
                                    return ownerWallet.getDataProvider().fetch(new Query<>())
                                            .filter(item -> item.getWalletId().equals(walletId))
                                            .findFirst()
                                            .orElseGet(() -> walletService.getWalletListItemById(walletId));
                                }
                        )
                .bind("walletOwnerId");

        binder.forField(sharesAmount).bind("sharesAmount");
        binder.forField(sharePrice)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimal -> bigDecimal == null ? null : bigDecimal.doubleValue(),
                        "Nieprawidłowa cena"
                )
                .bind("sharePrice");
        binder.forField(subscriptionEnd).bind("subscriptionEnd");
    }

    private void confirmNewIPO(){
        try{
            ipoDTO.setCompanyId(companyId);
            binder.writeBean(ipoDTO);

            ipoService.addIPO(ipoDTO);

            Notification.show("Rozpoczęto emisję", 4000, Notification.Position.TOP_CENTER);
            startIPOButton.setEnabled(canStartIPO());
            dialog.close();
        } catch (Exception e) {
            Notification.show("Wystąpił błąd: " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
        }
    }

    public void setCompany(Integer companyId) {
        this.companyId = companyId;
        startIPOButton.setEnabled(canStartIPO());
    }

    private boolean canStartIPO(){
        return !ipoService.hasActiveIPO(companyId);
    }
}
