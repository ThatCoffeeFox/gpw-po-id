package pl.gpwpoid.origin.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.CompanyRepository;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.services.CompanyService;

import java.util.Collection;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getTradableCompaniesId() {
        return companyRepository.findTradableCompaniesId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyListItem> getCompaniesViewList() {
        return companyRepository.getCompaniesAsViewItems();
    }
}
