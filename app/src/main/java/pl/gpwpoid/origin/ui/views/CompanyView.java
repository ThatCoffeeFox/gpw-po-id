package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.models.wallet.Wallet;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@Route("companies/:companyId")
@RolesAllowed({"admin","user"})
public class CompanyView extends VerticalLayout implements BeforeEnterObserver {
    private final CompanyService companyService;
    private final OrderService orderService;
    private final WalletsService walletsService;
    private Integer companyId;
    private Optional<Company> company;

    //pola
    ComboBox<String> orderType = new ComboBox<>("Order Type");
    ComboBox<Wallet> wallet = new ComboBox<>("Wallet");
    IntegerField amount = new IntegerField("Amount of shares");
    NumberField price = new NumberField("Price");
    DatePicker date = new DatePicker("Date");

    @Autowired
    public CompanyView(CompanyService companyService, OrderService orderService, WalletsService walletsService) {
        this.companyService = companyService;
        this.orderService = orderService;
        this.walletsService = walletsService;

        setSizeFull();

        ComboBox<String> orderType = new ComboBox<>("Order Type");
        orderType.setPlaceholder("Choose Order Type");
        orderType.setItems("sell", "buy");
        orderType.setRequiredIndicatorVisible(true);
        orderType.setErrorMessage("Order type is required");

        ComboBox<WalletListItem> walletId = new ComboBox<>("Wallet");
        walletId.setPlaceholder("Choose Wallet");
        Collection<WalletListItem> walletList;
        if(SecurityUtils.isLoggedIn()){
            walletList = walletsService.getWalletsForCurrentUser();
        }
        else{
            walletList = Collections.emptyList();
        }
        walletId.setItems(walletList);
        walletId.setItemLabelGenerator(WalletListItem::getName);
        walletId.setRequiredIndicatorVisible(true);
        walletId.setErrorMessage("Wallet is required");

        IntegerField shares = new IntegerField("Amount of shares");
        shares.setRequiredIndicatorVisible(true);
        shares.setErrorMessage("Amount of shares is required");

        NumberField price = new NumberField("Price");

        DatePicker orderExpiration = new DatePicker("Order expiration");

        Button submit = new Button("Submit", buttonClickEvent -> {
            String chosenOrderTypeString = orderType.getValue();
            OrderType chosenOrderType = new OrderType();
            chosenOrderType.setOrderType(chosenOrderTypeString);

            Integer chosenWalletId = walletId.getValue().getWalletId();
            Optional<Wallet> chosenWalletOptional = walletsService.getWalletById(chosenWalletId);
            Wallet chosenWallet;
            if(chosenWalletOptional.isPresent()){
                chosenWallet = chosenWalletOptional.get();
            }
            else{
                chosenWallet = null;
                showError("Wallet not found");
            }

            Integer chosenSharesAmount = shares.getValue();
            BigDecimal chosenPrice = BigDecimal.valueOf(price.getValue());
            Optional<Company> chosenCompanyOptional = companyService.getCompanyById(companyId);
            Company chosenCompany;
            if(chosenCompanyOptional.isPresent()){
                chosenCompany = chosenCompanyOptional.get();
            }
            else{
                chosenCompany = null;
                showError("Company not found");
            }

            LocalDate chosenExpirationDateLocal = orderExpiration.getValue();
            Date chosenExpirationDate = Date.from(chosenExpirationDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

            orderService.addOrder(chosenOrderType,chosenSharesAmount, chosenPrice, chosenWallet, chosenCompany, chosenExpirationDate);
        });

        add(orderType, walletId, shares, price, orderExpiration, submit);
    }



    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<String> CompanyIdParam = beforeEnterEvent.getRouteParameters().get("companyId");
        if (CompanyIdParam.isPresent()) {
            try{
                companyId = Integer.parseInt(CompanyIdParam.get());
                loadCompanyDetails();
            } catch (NumberFormatException e) {
                showError("Nieprawidłowy format ID firmy");
            }
        }
        else{
            showError("Nie podano ID firmy");
        }
    }

    private void loadCompanyDetails() {
        if(companyId != null) {
            company = companyService.getCompanyById(companyId);
            if(company.isEmpty()){
                showError("firma o ID " + companyId + " nie została znalezniona");
            }
        }
        else{
            showError("bral ID firmy");
        }
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }

}
