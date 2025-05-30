package pl.gpwpoid.origin.ui.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
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
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.repositories.views.WalletListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.services.TransactionService;
import pl.gpwpoid.origin.services.WalletsService;
import pl.gpwpoid.origin.ui.views.DTO.OrderDTO;
import pl.gpwpoid.origin.utils.SecurityUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Route(value = "companies", layout = MainLayout.class)
@AnonymousAllowed
public class CompanyView extends HorizontalLayout implements HasUrlParameter<Integer> {
    private final CompanyService companyService;
    private final OrderService orderService;
    private final WalletsService walletsService;
    private final TransactionService transactionService;
    private final Binder<OrderDTO> binder = new BeanValidationBinder<>(OrderDTO.class);
    private final ComboBox<OrderType> orderType = new ComboBox<>("Typ zlecenia");
    private final ComboBox<WalletDTO> wallet = new ComboBox<>("Portfel");
    private Integer companyId;
    private Company company;

    private Collection<WalletListItem> userWallets;
    private final Binder<OrderDTO> binder = new BeanValidationBinder<>(OrderDTO.class);
    private OrderDTO orderDTO;

    private final ComboBox<String> orderType = new ComboBox<>("Typ zlecenia");
    private final ComboBox<WalletListItem> wallet = new ComboBox<>("Portfel");
    private final IntegerField sharesAmount = new IntegerField("Ilość akcji");
    private final NumberField sharePrice = new NumberField("Cena za akcję");
    private final DateTimePicker orderExpirationDate = new DateTimePicker("Data wygaśnięcia zlecenia");
    private final Button submitButton = new Button("Złóż zlecenie");
    private final Grid<TransactionListItem> grid = new Grid<>();
    private final H3 table = new H3("Niedawne transakcje");
    private final Chart candlestickChart = new Chart(ChartType.CANDLESTICK);
    private final H3 chartHeader = new H3("Historia cen akcji");
    private Integer companyId;
    private Company company;
    private OrderDTO orderDTO;

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    @Autowired
    public CompanyView(CompanyService companyService, OrderService orderService, WalletsService walletsService, TransactionService transactionService) {
        this.companyService = companyService;
        this.orderService = orderService;
        this.walletsService = walletsService;
        this.transactionService = transactionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);


        configureCandlestickChart();
        VerticalLayout chartContainer = new VerticalLayout(chartHeader, candlestickChart);
        chartContainer.setWidth("100%");
        chartContainer.setAlignItems(Alignment.CENTER);
        candlestickChart.setWidth("100%");
        candlestickChart.setHeight("400px");


        HorizontalLayout gridAndFormContainer = new HorizontalLayout();
        gridAndFormContainer.setWidthFull();
        gridAndFormContainer.setAlignItems(Alignment.START);

        VerticalLayout gridLayout = configureGrid();
        gridLayout.getStyle().set("flex-grow", "1");

        gridAndFormContainer.add(gridLayout);
        if(SecurityUtils.isLoggedIn()){
            loadWalletListItems();
            FormLayout formLayout = createOrderPlacementForm();
            formLayout.getStyle().set("flex-grow", "1");
            gridAndFormContainer.add(formLayout);
            configureFields();
            configureSubmitButton();
        } else {


            gridAndFormContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        }


        removeAll();


        VerticalLayout mainPageLayout = new VerticalLayout();
        mainPageLayout.setSizeFull();
        mainPageLayout.add(chartContainer, gridAndFormContainer);
        mainPageLayout.setFlexGrow(1, chartContainer);
        mainPageLayout.setFlexGrow(2, gridAndFormContainer);

        add(mainPageLayout);
        setFlexGrow(1, mainPageLayout);
    }

    private void configureCandlestickChart() {
        Configuration conf = candlestickChart.getConfiguration();
        conf.setTitle("Wykres świecowy");


        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.DATETIME);


        conf.addxAxis(xAxis);


        YAxis yAxis = new YAxis();
        yAxis.setTitle("Cena");
        conf.addyAxis(yAxis);


        Tooltip tooltip = new Tooltip();
        tooltip.setDateTimeLabelFormats(new DateTimeLabelFormats());
        tooltip.setPointFormat("<span style=\"color:{point.color}\">●</span> <b> {series.name}</b><br/>" + "Otwarcie: {point.open}<br/>" + "Najwyższa: {point.high}<br/>" + "Najniższa: {point.low}<br/>" + "Zamknięcie: {point.close}<br/>");
        conf.setTooltip(tooltip);

        PlotOptionsCandlestick plotOptions = new PlotOptionsCandlestick();


        conf.setPlotOptions(plotOptions);
    }

    private void loadAndRenderCandlestickData() {
        if (companyId == null) {

            return;
        }


        Configuration conf = candlestickChart.getConfiguration();


        conf.setSeries(new ArrayList<>());


        List<OHLCDataItem> ohlcDataList = transactionService.getOHLCDataByCompanyId(companyId, LocalDateTime.now().minusDays(90), LocalDateTime.now());


        if (ohlcDataList == null || ohlcDataList.isEmpty()) {
            chartHeader.setText("Historia cen akcji (brak danych)");
            candlestickChart.setVisible(false);


            candlestickChart.drawChart();
            return;
        }

        candlestickChart.setVisible(true);
        chartHeader.setText("Historia cen akcji");


        DataSeries dataSeries = new DataSeries("Cena Akcji");

        for (OHLCDataItem data : ohlcDataList) {
            if (data.getTimestamp() == null || data.getOpen() == null || data.getHigh() == null || data.getLow() == null || data.getClose() == null) {
                System.err.println("loadAndRenderCandlestickData: Skipping OHLC item with null values: " + data);
                continue;
            }
            OhlcItem item = new OhlcItem();
            Instant instant = data.getTimestamp().toInstant();
            item.setX(instant);
            item.setOpen(data.getOpen());
            item.setHigh(data.getHigh());
            item.setLow(data.getLow());
            item.setClose(data.getClose());
            dataSeries.add(item);
        }

        if (!dataSeries.getData().isEmpty()) {
            conf.addSeries(dataSeries);

        } else {

            chartHeader.setText("Historia cen akcji (niekompletne dane)");
            candlestickChart.setVisible(false);
        }


        candlestickChart.drawChart();
    }

    private FormLayout createOrderPlacementForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(orderType, wallet, sharesAmount, sharePrice, orderExpirationDate, submitButton);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

        formLayout.setColspan(submitButton, 2);
        return formLayout;
    }

    private void bindFields() {
        this.orderDTO = new OrderDTO();
        binder.setBean(orderDTO);

        binder.forField(orderType).bind("orderType");
        binder.forField(wallet).bind("wallet");
        binder.forField(sharesAmount).bind("amount");
        binder.forField(sharePrice).withConverter(doubleValue -> {
            if (doubleValue == null) return null;
            return BigDecimal.valueOf(doubleValue);
        }, bigDecimal -> {
            if (bigDecimal == null) return null;
            return bigDecimal.doubleValue();
        }, "Nieprawidłowa cena").bind("price");
        binder.forField(orderExpirationDate).bind("dateTime");
        binder.forField(wallet)
                .withConverter(
                        WalletListItem::getWalletId,
                        walletId -> {
                            return userWallets.stream().filter(w -> w.getWalletId().equals(walletId))
                                    .findFirst().orElse(null);
                        },
                        "ERROR"
                )
                .bind("walletId");
        binder.forField(sharesAmount).bind("sharesAmount");
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
                .bind("sharePrice");
        binder.forField(orderExpirationDate).bind("orderExpirationDate");
        binder.setBean(orderDTO);
    }
    private void configureFields() {
        orderType.setPlaceholder("Wybierz typ zlecenia");
        orderType.setRequiredIndicatorVisible(true);
        orderType.setErrorMessage("Typ zlecenia jest wymagany");
        orderType.setItems("sell", "buy");
        orderType.setItemLabelGenerator(
                orderType -> {
                    if(orderType.equals("sell"))
                        return "Sprzedaj";
                    if(orderType.equals("buy"))
                        return "Kup";
                    return null;
                }
        );

        wallet.setRequiredIndicatorVisible(true);
        wallet.setErrorMessage("Portfel jest wymagany");
        wallet.setItems(userWallets);
        wallet.setItemLabelGenerator(
                wallet -> {
                    return wallet.getName() +
                            " (" +
                            FUNDS_FORMATTER.format(wallet.getFunds()) +
                            " zł)";
                }
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
                if (order.getCompany() == null) {
                    if (company != null) order.setCompany(company);
                    else showError("Brak firmy");
                }
                binder.writeBean(order);
                orderService.addOrder(order);
                Notification.show("Złożono zlecenie", 4000, Notification.Position.TOP_CENTER);

                OrderDTO nextOrder = new OrderDTO();
                binder.setBean(nextOrder);
                orderType.clear();
                wallet.clear();
            } catch (ValidationException e) {
                Notification.show("Niepoprawne dane.", 4000, Notification.Position.TOP_CENTER);
            } catch (Exception e) {
                Notification.show("Wystąpił błąd podczas składania zlecenia: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER);
            }
        });
    }


    private VerticalLayout configureGrid() {
        table.setWidth("100%");
        table.getStyle().set("text-align", "center");
        table.getStyle().set("margin-bottom", "10px");
        grid.setAllRowsVisible(true);
        grid.setWidth("100%");
        grid.addColumn(TransactionListItem::getDate).setHeader("Data");
        grid.addColumn(TransactionListItem::getSharesAmount).setHeader("Ilość sprzedanych akcji");
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pl", "PL"));
        grid.addColumn(new NumberRenderer<>(TransactionListItem::getSharePrice, currency, "brak")).setHeader("Cena").setTextAlign(ColumnTextAlign.END).setSortable(true);
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.add(table, grid);
        gridLayout.setAlignItems(Alignment.CENTER);
        gridLayout.setPadding(false);
        gridLayout.setSpacing(false);
        gridLayout.setMaxWidth("700px");
        return gridLayout;
    }


    private void loadTransactionListItems() {
        Collection<TransactionListItem> transactionList = transactionService.getCompanyTransactionsById(companyId, 5);
        grid.setItems(transactionList);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer parameter) {
        this.companyId = parameter;
        try {
            loadCompanyDetails();
            loadTransactionListItems();
            if (SecurityUtils.isLoggedIn()) {

                bindFields();

            }
            loadAndRenderCandlestickData();
        } catch (Exception e) {
            System.err.println("ERROR in setParameter for companyId " + companyId + ":");
            e.printStackTrace();
            showError("Wystąpił błąd podczas ładowania danych. Szczegóły w logach serwera. Komunikat: " + e.getMessage());
        }
    }

    private void loadCompanyDetails() {
        if (companyId != null) {
            Optional<Company> currentCompany = companyService.getCompanyById(companyId);
            if (currentCompany.isPresent()) {
                company = currentCompany.get();
                OrderDTO newOrderDTO = new OrderDTO();
                newOrderDTO.setCompany(company);
            } else {
                showError("firma o ID " + companyId + " nie została znalezniona");
            }
        } else {
            showError("brak ID firmy");
        }
    }

    private void loadWalletListItems(){
        userWallets = walletsService.getWalletListViewForCurrentUser();
    }

    private void showError(String message) {
        add(new Text(message));
        Notification.show(message, 4000, Notification.Position.MIDDLE);
    }
}
