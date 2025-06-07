package pl.gpwpoid.origin.ui.views.AdminCompanyView;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.gpwpoid.origin.repositories.views.TransactionListItem;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.services.TransactionService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;

public class AdminCompanyTransactionsGrid extends VerticalLayout {
    private final TransactionService transactionService;
    private Integer companyId;

    private final Grid<TransactionListItem> grid = new Grid<>();

    private static final DecimalFormat FUNDS_FORMATTER = new DecimalFormat(
            "#,##0.00",
            DecimalFormatSymbols.getInstance(new Locale("pl", "PL"))
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public AdminCompanyTransactionsGrid(TransactionService transactionService) {
        this.transactionService = transactionService;

        add(new H3("Historia transakcji"), grid);
        configureGrid();
    }

    private void configureGrid() {
        grid.addColumn(this::formatFunds)
                .setHeader("Cena akcji")
                .setAutoWidth(true);

        grid.addColumn(TransactionListItem::getSharesAmount)
                .setHeader("Ilość akcji")
                .setAutoWidth(true);

        grid.addColumn(this::formatDate)
                .setHeader("Data")
                .setAutoWidth(true);

        grid.setPageSize(10);
    }

    private String formatFunds(TransactionListItem item) {
        if(item.getSharePrice() == null)
            return "";
        return FUNDS_FORMATTER.format(item.getSharePrice()) + " zł";
    }

    private String formatDate(TransactionListItem item) {
        if(item.getDate() == null)
            return "";
        LocalDateTime date = item.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date.format(DATE_FORMATTER);
    }

    public void setCompany(Integer companyId) {
        this.companyId = companyId;
    }

    public void updateList(){
        Collection<TransactionListItem> items = transactionService.getCompanyTransactionsById(companyId, 50);
        grid.setItems(items);
    }
}
