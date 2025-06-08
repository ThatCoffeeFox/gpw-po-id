package pl.gpwpoid.origin.ui.views.adminAccountView;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.data.domain.PageRequest;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.order.Order;
import pl.gpwpoid.origin.models.order.Transaction;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.DTO.ActiveOrderDTO;
import pl.gpwpoid.origin.repositories.views.AccountListItem;
import pl.gpwpoid.origin.repositories.views.ActiveOrderListItem;
import pl.gpwpoid.origin.repositories.views.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.*;
import pl.gpwpoid.origin.ui.views.DTO.AdminProfileUpdateDTO;
import pl.gpwpoid.origin.ui.views.MainLayout;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@Route(value = "admin/accounts", layout = MainLayout.class)
@PageTitle("Szczegóły Użytkownika")
@RolesAllowed("admin")
public class AdminUserDetailView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final AccountService accountService;
    private final WalletsService walletService;
    private final OrderService orderService;
    private final TransactionService transactionService;

    private Integer currentAccountId;

    private final H2 header = new H2("Zarządzanie Użytkownikiem");
    private final Binder<AdminProfileUpdateDTO> binder = new Binder<>(AdminProfileUpdateDTO.class);

    
    private final TextField firstNameField = new TextField("Imię");
    private final TextField secondaryNameField = new TextField("Drugie imię");
    private final TextField lastNameField = new TextField("Nazwisko");
    private final TextField emailField = new TextField("Email");
    private final TextField townField = new TextField("Miasto");
    private final TextField postalCodeField = new TextField("Kod pocztowy");
    private final TextField streetField = new TextField("Ulica i numer");
    private final TextField phoneNumberField = new TextField("Numer telefonu");
    private final TextField peselField = new TextField("PESEL");

    
    private final Button saveButton = new Button("Zapisz zmiany", e -> saveChanges());

    
    private final Grid<AccountListItem> historyGrid = new Grid<>(AccountListItem.class);
    private final Grid<WalletListItem> walletsGrid = new Grid<>(WalletListItem.class);
    private final Grid<ActiveOrderListItem> ordersGrid = new Grid<>(ActiveOrderListItem.class); 
    private final Grid<TransactionDTO> transactionsGrid = new Grid<>(TransactionDTO.class); 

    public AdminUserDetailView(AccountService accountService, WalletsService walletService, OrderService orderService, TransactionService transactionService) {
        this.accountService = accountService;
        this.walletService = walletService;
        this.orderService = orderService;
        this.transactionService = transactionService;

        configureForm();
        configureGrids();

        add(
                header,
                createFormLayout(),
                new H3("Historia zmian danych"), historyGrid,
                new H3("Portfele użytkownika"), walletsGrid,
                new H3("Ostatnie zlecenia"), ordersGrid,
                new H3("Ostatnie transakcje"), transactionsGrid
        );
    }

    private void configureForm() {
        
        firstNameField.setRequired(true);
        lastNameField.setRequired(true);

        
        emailField.setReadOnly(true);
        townField.setReadOnly(true);
        postalCodeField.setReadOnly(true);
        streetField.setReadOnly(true);
        phoneNumberField.setReadOnly(true);
        peselField.setReadOnly(true);

        
        binder.forField(firstNameField).asRequired().bind(AdminProfileUpdateDTO::getFirstName, AdminProfileUpdateDTO::setFirstName);
        binder.forField(secondaryNameField).bind(AdminProfileUpdateDTO::getSecondaryName, AdminProfileUpdateDTO::setSecondaryName);
        binder.forField(lastNameField).asRequired().bind(AdminProfileUpdateDTO::getLastName, AdminProfileUpdateDTO::setLastName);
    }

    private FormLayout createFormLayout() {
        FormLayout form = new FormLayout(firstNameField, secondaryNameField, lastNameField, emailField, phoneNumberField, peselField, townField, postalCodeField, streetField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton);
        add(form, buttonLayout);
        return form;
    }

    private void configureGrids() {
        
        historyGrid.removeAllColumns();
        historyGrid.addColumn(AccountListItem::getAccountId).setHeader("ID").setFlexGrow(1);
        historyGrid.addColumn(info -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(info.getUpdatedAt())).setHeader("Data zmiany").setSortable(true);
        historyGrid.addColumn(AccountListItem::getFirstName).setHeader("Imię");
        historyGrid.addColumn(AccountListItem::getLastName).setHeader("Nazwisko");
        historyGrid.addColumn(AccountListItem::getTownName).setHeader("Miejscowość").setSortable(true).setFlexGrow(2);
        historyGrid.addColumn(AccountListItem::getPostalCodeValue).setHeader("Kod Pocztowy").setSortable(true).setFlexGrow(2);
        historyGrid.addColumn(AccountListItem::getStreet).setHeader("Ulica").setSortable(true).setFlexGrow(2);
        historyGrid.addColumn(AccountListItem::getEmail).setHeader("Email");
        historyGrid.addColumn(AccountListItem::getPhoneNumber).setHeader("Telefon");
        historyGrid.addColumn(AccountListItem::getPesel).setHeader("PESEL");

        
        walletsGrid.removeAllColumns();
        walletsGrid.addColumn(WalletListItem::getWalletId).setHeader("ID Portfela");
        walletsGrid.addColumn(WalletListItem::getName).setHeader("Nazwa");
        walletsGrid.addColumn(dto -> String.format("%.2f PLN", dto.getFunds())).setHeader("Cena").setFlexGrow(1);

        ordersGrid.removeAllColumns();
        ordersGrid.addColumn(ActiveOrderListItem::getOrderId).setHeader("ID").setSortable(true).setFlexGrow(0);

        ordersGrid.addColumn(ActiveOrderListItem::getOrderType).setHeader("Typ").setSortable(true).setFlexGrow(1);
        ordersGrid.addColumn(ActiveOrderListItem::getSharesAmount).setHeader("Ilość").setFlexGrow(1);
        ordersGrid.addColumn(dto -> String.format("%.2f PLN", dto.getSharePrice())).setHeader("Cena").setFlexGrow(1);
        ordersGrid.addColumn(dto -> new SimpleDateFormat("yyyy-MM-dd HH:mm").format(dto.getOrderStartDate())).setHeader("Data złożenia").setSortable(true).setFlexGrow(1);
        ordersGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        transactionsGrid.removeAllColumns();

        transactionsGrid.addColumn(dto -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dto.getDate())).setHeader("Data").setSortable(true).setFlexGrow(1);
        transactionsGrid.addColumn(TransactionDTO::getSharesAmount).setHeader("Ilość").setFlexGrow(1);
        transactionsGrid.addColumn(dto -> String.format("%.2f PLN", dto.getSharePrice())).setHeader("Cena").setFlexGrow(1);
        transactionsGrid.addColumn(dto -> String.format("%.2f PLN", dto.getSharePrice().multiply(BigDecimal.valueOf(dto.getSharesAmount()))))
                .setHeader("Wartość").setFlexGrow(1);
        transactionsGrid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    @Override
    public void setParameter(BeforeEvent event, Integer parameter) {
        this.currentAccountId = parameter;
        loadData();
    }

    private void loadData() {
        if (currentAccountId == null) return;

        
        AccountInfo latestInfo = accountService.getNewestAccountInfoItemById(currentAccountId);
        if (latestInfo != null) {
            header.setText("Zarządzanie Użytkownikiem: " + latestInfo.getFirstName() + " " + latestInfo.getLastName());

            
            emailField.setValue(latestInfo.getEmail());
            phoneNumberField.setValue(latestInfo.getPhoneNumber());
            peselField.setValue(latestInfo.getPesel());
            if (latestInfo.getPostalCodesTowns() != null) {
                townField.setValue(latestInfo.getPostalCodesTowns().getTown().getName());
                postalCodeField.setValue(latestInfo.getPostalCodesTowns().getPostalCode().getPostalCode());
                streetField.setValue(String.format("%s %s/%s", latestInfo.getStreet(), latestInfo.getStreetNumber(), latestInfo.getApartmentNumber()).replace("null", "").trim());
            }

            
            AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO(
                    currentAccountId,
                    latestInfo.getFirstName(),
                    latestInfo.getSecondaryName(),
                    latestInfo.getLastName()
            );
            binder.setBean(dto);
        }

        
        historyGrid.setItems(accountService.getAccountInfoHistory(currentAccountId));
        walletsGrid.setItems(walletService.getWalletListViewByAccountId(currentAccountId));
        
        ordersGrid.setItems(orderService.getOrderListItemsByAccountId(currentAccountId, PageRequest.of(0, 10)));
        transactionsGrid.setItems(transactionService.getLatestTransactionsByAccountId(currentAccountId, PageRequest.of(0, 20)));
    }

    private void saveChanges() {
        if (binder.validate().isOk()) {
            try {
                AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
                binder.writeBean(dto);
                dto.setAccountId(currentAccountId); 

                accountService.updateAccountByAdmin(dto);
                Notification.show("Dane użytkownika zostały zaktualizowane.", 3000, Notification.Position.TOP_CENTER);
                loadData(); 
            } catch (Exception e) {
                Notification.show("Błąd podczas zapisu: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        }
    }
}