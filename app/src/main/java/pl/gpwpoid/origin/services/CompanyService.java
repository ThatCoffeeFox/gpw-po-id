package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    List<Integer> getTradableCompaniesId();

    List<CompanyListItem> getCompaniesViewList();

    CompanyListItem getCompanyItemById(int companyId);

    Optional<Company> getCompanyById(int companyId);
}
