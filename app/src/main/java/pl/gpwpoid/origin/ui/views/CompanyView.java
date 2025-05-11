package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.order.OrderType;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;
import pl.gpwpoid.origin.ui.views.DTO.WalletDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;

@Route("companies")
@AnonymousAllowed
public class CompanyView extends HorizontalLayout implements HasUrlParameter<Integer>{
    private final CompanyService companyService;
    private final OrderService orderService;
    private final WalletsService walletsService;
    private final TransactionService transactionService;
    private Integer companyId;
    private Company company;

    private final Binder<OrderDTO> binder = new BeanValidationBinder<>(OrderDTO.class);
    private OrderDTO orderDTO;

    //pola
    private final ComboBox<OrderType> orderType = new ComboBox<>("Typ zlecenia");
    private final ComboBox<WalletDTO> wallet = new ComboBox<>("Portfel");
    private final IntegerField sharesAmount = new IntegerField("Ilość akcji");
    private final NumberField sharePrice = new NumberField("Cena za akcję");
    private final DateTimePicker orderExpirationDate = new DateTimePicker("Data wygaśnięcia zlecenia");
    private final Button submitButton = new Button("Złóż zlecenie");

    private final Grid<TransactionListItem> grid = new Grid<>();
    private final H3 table = new H3("Niedawne transakcje");

    @Autowired
    public CompanyView(CompanyService companyService, OrderService orderService, WalletsService walletsService, TransactionService transactionService) {
        this.companyService = companyService;
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.transactionService = transactionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.START);

        if(SecurityUtils.isLoggedIn()){
            FormLayout formLayout = createOrderPlacementForm();
            VerticalLayout gridLayout = configureGrid();
            add(gridLayout, formLayout);
            setFlexGrow(1, formLayout);
            setFlexGrow(1, gridLayout);
            configureFields();
            configureSubmitButton();
        }
        else{
            VerticalLayout gridLayout = configureGrid();
            add(gridLayout);
        }
    }

    private FormLayout createOrderPlacementForm(){
        FormLayout formLayout = new FormLayout();
        formLayout.add(orderType, wallet, sharesAmount, sharePrice, orderExpirationDate, submitButton);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        //formLayout.setWidth("700px");
        formLayout.setColspan(submitButton, 2);
        return formLayout;
    }

    private void bindFields(){
        this.orderDTO = new OrderDTO();
        orderDTO.setCompany(company);
        binder.setBean(orderDTO);

        binder.forField(orderType).bind("orderType");
        binder.forField(wallet).bind("wallet");
        binder.forField(sharesAmount).bind("amount");
        binder.forField(sharePrice)
                .withConverter(
                        doubleValue -> {
                            if(doubleValue == null)
                                return null;
                            return BigDecimal.valueOf(doubleValue);
                        },
                        bigDecimal -> {
                            if(bigDecimal == null)
                                return null;
                            return bigDecimal.doubleValue();
                        },
                        "Nieprawidłowa cena"
                )
                .bind("price");
        binder.forField(orderExpirationDate).bind("dateTime");

        binder.setBean(orderDTO);
    }

    private void configureFields(){
        orderType.setPlaceholder("Wybierz typ zlecenia");
        orderType.setRequiredIndicatorVisible(true);
        orderType.setErrorMessage("Typ zlecenia jest wymagany");
        OrderType sellOrder = new OrderType();
        sellOrder.setOrderType("sell");
        OrderType buyOrder = new OrderType();
        buyOrder.setOrderType("buy");
        orderType.setItems(sellOrder, buyOrder);
        orderType.setItemLabelGenerator(OrderType::getOrderType);

        wallet.setPlaceholder("Wybierz portfel");
        wallet.setRequiredIndicatorVisible(true);
        wallet.setErrorMessage("Portfel jest wymagany");
        Collection<WalletDTO> wallets;
        if(SecurityUtils.isLoggedIn()){
            wallets = walletsService.getWalletDTOForCurrentUser();
        }
        else{
            wallets = Collections.emptyList();
        }
        wallet.setItems(wallets);
        wallet.setItemLabelGenerator(WalletDTO::getName);

        sharesAmount.setPlaceholder("Wpisz ilość akcji");
        sharesAmount.setRequiredIndicatorVisible(true);
        sharesAmount.setErrorMessage("Ilość akcji jest wymagana");

        sharePrice.setPlaceholder("Wpisz cenę za akcję");

        orderExpirationDate.setLocale(new Locale("pl", "PL"));
        orderExpirationDate.setStep(Duration.ofMinutes(1));
        //orderExpirationDate.setMin(LocalDateTime.now());
    }

    private void configureSubmitButton(){
        submitButton.addClickListener(event -> {
            try {
                OrderDTO order = binder.getBean();
                if(order.getCompany() == null){
                    if(company != null)
                        order.setCompany(company);
                    else
                        showError("Brak firmy");
                }
                binder.writeBean(order);
                orderService.addOrder(order);
                Notification.show("Złożono zlecenie", 4000, Notification.Position.TOP_CENTER);
                OrderDTO nextOrder = new OrderDTO();
                nextOrder.setCompany(company);
                binder.setBean(nextOrder);
                orderType.clear();
                wallet.clear();
            } catch (ValidationException e) {
                Notification.show("Niepoprawne dane.", 4000, Notification.Position.TOP_CENTER);
            } catch (Exception e){
                Notification.show("Wystąpił błąd podczas składania zlecenia: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        });
    }

    private VerticalLayout configureGrid(){
        table.setWidth("100%");
        table.getStyle().set("text-align", "center");
        table.getStyle().set("margin-bottom", "10px");
        grid.setAllRowsVisible(true);
        grid.setWidth("100%");
        grid.addColumn(TransactionListItem::getDate).setHeader("Data");
        grid.addColumn(TransactionListItem::getSharesAmount).setHeader("Ilość sprzedanych akcji");
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pl","PL"));
        grid.addColumn(new NumberRenderer<>(TransactionListItem::getSharePrice, currency, "brak"))
                .setHeader("Cena")
                .setTextAlign(ColumnTextAlign.END).setSortable(true);
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.add(table, grid);
        gridLayout.setAlignItems(Alignment.CENTER);
        gridLayout.setPadding(false);
        gridLayout.setSpacing(false);
        gridLayout.setMaxWidth("700px");
        return gridLayout;
    }


    private void loadTransactionListItems(){
        Collection<TransactionListItem> transactionList = transactionService.getCompanyTransactionsById(companyId, 5);
        grid.setItems(transactionList);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.companyId = parameter;
        try{
            loadTransactionListItems();
            loadCompanyDetails();
            bindFields();
        } catch (Exception e){
            showError("Nieprawidłowy adres: " + e.getMessage());
        }
    }

    private void loadCompanyDetails() {
        if(companyId != null) {
            Optional<Company> currentCompany = companyService.getCompanyById(companyId);
            if(currentCompany.isPresent()){
                company = currentCompany.get();
                OrderDTO newOrderDTO = new OrderDTO();
                newOrderDTO.setCompany(company);
            }
            else{
                showError("firma o ID " + companyId + " nie została znalezniona");
            }
        }
        else{
            showError("brak ID firmy");
        }
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }
}
