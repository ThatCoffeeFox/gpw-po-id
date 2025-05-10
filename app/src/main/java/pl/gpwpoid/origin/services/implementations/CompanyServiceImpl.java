package pl.gpwpoid.origin.services.implementations;

import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.CompanyRepository;
import pl.gpwpoid.origin.services.CompanyService;

import java.util.Collection;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }
    @Override
    public List<Integer> getTradableCompaniesId() {
        return companyRepository.findTradableCompaniesId();
    }
}
