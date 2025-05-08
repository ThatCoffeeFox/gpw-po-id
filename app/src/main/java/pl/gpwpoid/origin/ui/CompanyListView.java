package pl.gpwpoid.origin.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pl.gpwpoid.origin.company.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Route("companies")
@PageTitle("Companies List")

public class CompanyListView extends VerticalLayout {

    private final CompanyRepository accountRepository;
    private Grid<Company> grid = new Grid<>(Company.class, false);

    public CompanyListView(CompanyRepository accountRepository) {
        this.accountRepository = accountRepository;
        setSizeFull();
        configureGrid();
        add(grid);
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();



        grid.addColumn(Company::getId).setHeader("ID").setSortable(true);
        grid.addColumn(company -> getLatestInfo(company).map(CompanyInfo::getCode).orElse(null)).setHeader("code").setSortable(true);
        grid.addColumn(company -> getLatestInfo(company).map(CompanyInfo::getName).orElse(null)).setHeader("name").setSortable(true);
        grid.addColumn(this::getTotalShares).setHeader("Shares").setSortable(true);
        grid.addColumn(company -> getCurrentStatus(company).map(CompanyStatus::getTradable)).setHeader("Tradable Status").setSortable(true);


        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void updateList() {

        List<Company> companies = accountRepository.findAll();

        grid.setItems(companies);
    }

    private Optional<CompanyInfo> getLatestInfo(Company company) {
        return company.getCompanyInfos().stream()
                .max(Comparator.comparing(info -> info.getInfoId().getUpdatedAt()));
    }

    private Integer getTotalShares(Company company) {
        return company.getIpos().stream().flatMapToInt(ipo -> IntStream.of(ipo.getSharesAmount())).sum();
    }

    private Optional<CompanyStatus> getCurrentStatus(Company company) {
        return company.getCompanyStatuses().stream().max(Comparator.comparing(status -> status.getId().getDate()));
    }

}