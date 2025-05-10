package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.repositories.views.CompanyListItem;

import java.util.List;

public interface CompanyService {
    public List<Integer> getTradableCompaniesId();
    public List<CompanyListItem> getCompaniesViewList();
}
