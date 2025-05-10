package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    public List<Integer> getTradableCompaniesId();
    public List<CompanyListItem> getCompaniesViewList();
    public CompanyListItem getCompanyItemById(int companyId);
    public Optional<Company> getCompanyById(int companyId);
}
