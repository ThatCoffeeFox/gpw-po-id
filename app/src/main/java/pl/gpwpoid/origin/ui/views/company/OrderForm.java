package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;


public class OrderForm extends FormLayout {
    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );
    private final ComboBox<String> orderType = new ComboBox<>("Typ zlecenia");
    private final ComboBox<WalletListItem> wallet = new ComboBox<>("Portfel");
    private final IntegerField sharesAmount = new IntegerField("Ilość akcji");
    private final NumberField sharePrice = new NumberField("Cena za akcję");
    private final DateTimePicker orderExpirationDate = new DateTimePicker("Data wygaśnięcia zlecenia");
    private final Button submitButton = new Button("Złóż zlecenie");
    private final Binder<OrderDTO> binder = new BeanValidationBinder<>(OrderDTO.class);
    private final OrderService orderService;
    private final WalletsService walletsService;
    private Collection<WalletListItem> userWallets;
    private Integer companyId;

    public OrderForm(OrderService orderService, WalletsService walletsService) {
        this.orderService = orderService;
        this.walletsService = walletsService;

        add(orderType, wallet, sharesAmount, sharePrice, orderExpirationDate, submitButton);
        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );
        setColspan(submitButton, 2);

        if (SecurityUtils.isLoggedIn()) {
            loadWalletListItems();
            configureFields();
            configureSubmitButton();
            bindFields();
        }
    }

    private void bindFields() {
        OrderDTO orderDTO = new OrderDTO();
        binder.setBean(orderDTO);

        binder.forField(orderType).bind("orderType");
        binder.forField(wallet)
                .withConverter(
                        WalletListItem::getWalletId,
                        walletId -> userWallets.stream()
                                .filter(w -> w.getWalletId().equals(walletId))
                                .findFirst().orElse(null),
                        "ERROR"
                )
                .bind("walletId");
        binder.forField(sharesAmount).bind("sharesAmount");
        binder.forField(sharePrice)
                .withConverter(
                        doubleValue -> doubleValue == null ? null : BigDecimal.valueOf(doubleValue),
                        bigDecimal -> bigDecimal == null ? null : bigDecimal.doubleValue(),
                        "Nieprawidłowa cena"
                )
                .bind("sharePrice");
        binder.forField(orderExpirationDate).bind("orderExpirationDate");
    }

    private void configureFields() {
        orderType.setRequiredIndicatorVisible(true);
        orderType.setErrorMessage("Typ zlecenia jest wymagany");
        orderType.setItems("sell", "buy");
        orderType.setItemLabelGenerator(
                orderType -> {
                    if (orderType.equals("sell")) return "Sprzedaj";
                    if (orderType.equals("buy")) return "Kup";
                    return null;
                }
        );

        wallet.setRequiredIndicatorVisible(true);
        wallet.setErrorMessage("Portfel jest wymagany");
        wallet.setItems(userWallets);
        wallet.setItemLabelGenerator(
                wallet -> wallet.getName() + " (" + FUNDS_FORMATTER.format(wallet.getFunds()) + " zł)"
        );

        sharesAmount.setRequiredIndicatorVisible(true);
        sharesAmount.setErrorMessage("Ilość akcji jest wymagana");

        orderExpirationDate.setLocale(new Locale("pl", "PL"));
        orderExpirationDate.setStep(Duration.ofMinutes(1));
    }

    private void configureSubmitButton() {
        submitButton.addClickListener(event -> {
            try {
                OrderDTO order = binder.getBean();
                binder.writeBean(order);
                orderService.addOrder(order);
                Notification.show("Złożono zlecenie", 4000, Notification.Position.TOP_CENTER);

                OrderDTO nextOrder = new OrderDTO();
                binder.setBean(nextOrder);
                binder.getBean().setCompanyId(companyId);
                orderType.clear();
                wallet.clear();
            } catch (ValidationException e) {
                Notification.show("Niepoprawne dane", 4000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Wystąpił błąd podczas składania zlecenia: " + e.getMessage(),
                        4000, Notification.Position.TOP_CENTER);
            }
        });
    }

    private void loadWalletListItems() {
        Integer accountId = SecurityUtils.getAuthenticatedAccountId();
        userWallets = walletsService.getWalletListViewByAccountId(accountId);
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        binder.getBean().setCompanyId(companyId);
    }
}