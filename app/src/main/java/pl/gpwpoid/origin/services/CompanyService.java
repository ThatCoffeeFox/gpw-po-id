package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.views.AdminCompanyListItem;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.ui.views.DTO.CompanyDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyService {
    List<Integer> getTradableCompaniesId();
    List<CompanyListItem> getCompaniesViewList();
    CompanyListItem getCompanyItemById(int companyId);
    Optional<Company> getCompanyById(int companyId);
    Collection<AdminCompanyListItem> getAdminCompanyListItems();
    void setTradable(Integer companyId, Boolean tradable);
    void addCompany(CompanyDTO companyDTO);
    void updateCompany(CompanyDTO companyDTO);

}
