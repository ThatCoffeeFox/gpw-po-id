package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
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
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;
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

    private Binder<OrderDTO> binder = new BeanValidationBinder<>(OrderDTO.class);
    private OrderDTO orderDTO;

    //pola
    private ComboBox<OrderType> orderType = new ComboBox<>("Order Type");
    private ComboBox<Wallet> wallet = new ComboBox<>("Wallet");
    private IntegerField sharesAmount = new IntegerField("Amount of shares");
    private NumberField sharePrice = new NumberField("Price");
    private DatePicker orderExpirationDate = new DatePicker("Date");
    private Button submitButton = new Button("Złóż zlecenie");

    @Autowired
    public CompanyView(CompanyService companyService, OrderService orderService, WalletsService walletsService) {
        this.companyService = companyService;
        this.orderService = orderService;
        this.walletsService = walletsService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        FormLayout formLayout =createOrderPlacementForm();
        bindFields();
        configureFields();
        configureSubmitButton();
    }

    private FormLayout createOrderPlacementForm(){
        FormLayout formLayout = new FormLayout();
        formLayout.add(orderType, wallet, sharesAmount, sharePrice, orderExpirationDate);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setWidth("700px");
        formLayout.setColspan(submitButton, 2);
        return formLayout;
    }

    private void bindFields(){
        this.orderDTO = new OrderDTO();
        binder.setBean(orderDTO);

        binder.forField(orderType).bind("Typ zlecenia");
        binder.forField(wallet).bind("Portfel");
        binder.forField(sharesAmount).bind("Liczba akcji");
        binder.forField(sharePrice).bind("Cena za akcję");
        binder.forField(orderExpirationDate).bind("Moment wygaśnięcia zlecenia");

        binder.setBean(orderDTO);
    }

    private void configureFields(){
        //orderType
        orderType.setPlaceholder("Wybierz typ zlecenia");
        orderType.setRequiredIndicatorVisible(true);
        orderType.setErrorMessage("Typ zlecenia jest wymagany");
        OrderType sellOrder = new OrderType();
        sellOrder.setOrderType("sell");
        OrderType buyOrder = new OrderType();
        buyOrder.setOrderType("buy");
        orderType.setItems(sellOrder, buyOrder);

        wallet.setPlaceholder("Wybierz portfel");
        wallet.setRequiredIndicatorVisible(true);
        wallet.setErrorMessage("Portfel jest wymagany");
        Collection<Wallet> wallets;
        if(SecurityUtils.isLoggedIn()){
            wallets = walletsService.getWallets();
        }
        else{
            wallets = Collections.emptyList();
        }
        wallet.setItems(wallets);
        wallet.setItemLabelGenerator(Wallet::getName);

        sharesAmount.setPlaceholder("Wpisz ilość akcji");
        sharesAmount.setRequiredIndicatorVisible(true);
        sharesAmount.setErrorMessage("Ilość akcji jest wymagana");

        sharePrice.setPlaceholder("Wpisz cenę za akcję");

        orderExpirationDate.setPlaceholder("Wybierz datę wygaśnięcia zlecenia");
    }

    private void configureSubmitButton(){
        submitButton.addClickListener(event -> {
            try {
                binder.writeBean(orderDTO);
                orderService.addOrder(orderDTO);
                Notification.show("Złożono zlecenie", 4000, Notification.Position.TOP_CENTER);
                this.orderDTO = new OrderDTO();
                binder.readBean(orderDTO);
                orderType.clear();
                wallet.clear();
            } catch (ValidationException e) {
                Notification.show("Niepoprawne dane.", 4000, Notification.Position.TOP_CENTER);
            } catch (Exception e){
                Notification.show("Wystąpił błąd podczas składania zlecenia: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        });
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
