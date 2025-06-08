package pl.gpwpoid.origin.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.gpwpoid.origin.factories.CompanyFactory;
import pl.gpwpoid.origin.factories.CompanyStatusFactory;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.models.company.CompanyStatus;
import pl.gpwpoid.origin.repositories.CompanyRepository;
import pl.gpwpoid.origin.repositories.CompanyStatusRepository;
import pl.gpwpoid.origin.repositories.views.AdminCompanyListItem;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;
import pl.gpwpoid.origin.repositories.views.CompanyStatusItem;
import pl.gpwpoid.origin.services.AddressService;
import pl.gpwpoid.origin.services.CompanyService;
import pl.gpwpoid.origin.ui.views.DTO.CompanyDTO;
import pl.gpwpoid.origin.ui.views.DTO.CompanyUpdateDTO;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyStatusFactory companyStatusFactory;
    private final CompanyStatusRepository companyStatusRepository;
    private final AddressService addressService;
    private final CompanyFactory companyFactory;

    @Autowired
    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            CompanyStatusFactory companyStatusFactory,
            CompanyStatusRepository companyStatusRepository,
            AddressService addressService,
            CompanyFactory companyFactory) {
        this.companyStatusFactory = companyStatusFactory;
        this.companyRepository = companyRepository;
        this.companyStatusRepository = companyStatusRepository;
        this.addressService = addressService;
        this.companyFactory = companyFactory;
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

    @Override
    public CompanyListItem getCompanyItemById(int companyId) {
        return companyRepository.getCompanyById(companyId);
    }

    @Override
    public Optional<Company> getCompanyById(int companyId) {
        return companyRepository.findById(Long.valueOf(companyId));
    }

    @Override
    public Collection<AdminCompanyListItem> getAdminCompanyListItems() {
        return companyRepository.getAdminCompanyListItems();
    }

    @Override
    public void setTradable(Integer companyId, Boolean tradable) {
        Company company = companyRepository.findById(Long.valueOf(companyId)).orElse(null);
        CompanyStatus companyStatus = companyStatusFactory.createCompanyStatus(company, tradable);
        companyStatusRepository.save(companyStatus);
    }

    @Override
    public void addCompany(CompanyDTO companyDTO) {
        Town town = addressService.getTownById(companyDTO.getTownId()).get();
        PostalCodesTowns postalCodesTowns = addressService.getPostalCodesTowns(town, companyDTO.getPostalCode()).get();
        Company newCompany = companyFactory.createCompany(
                companyDTO.getCompanyName(),
                companyDTO.getCompanyCode(),
                companyDTO.getStreet(),
                companyDTO.getStreetNumber(),
                companyDTO.getApartmentNumber(),
                postalCodesTowns
        );
        companyRepository.save(newCompany);
    }

    @Override
    @Transactional
    public void updateCompany(CompanyUpdateDTO companyUpdateDTO) {
        if (companyUpdateDTO == null || !companyRepository.existsById(Long.valueOf(companyUpdateDTO.getCompanyId())))
            throw new IllegalArgumentException("Id nie istnieje lub jest NULL");

        Company company = companyRepository.findById(Long.valueOf(companyUpdateDTO.getCompanyId())).orElse(null);
        if (company == null)
            throw new IllegalArgumentException("Nie znaleziono Firmy");

        Town town = addressService.getTownById(companyUpdateDTO.getTownId()).get();
        PostalCodesTowns postalCodesTowns = addressService.getPostalCodesTowns(town, companyUpdateDTO.getPostalCode()).get();

        CompanyInfo updatedCompanyInfo = companyFactory.createCompanyInfo(companyUpdateDTO, postalCodesTowns);
        updatedCompanyInfo.setCompany(company);
        company.getCompanyInfos().add(updatedCompanyInfo);
        companyRepository.save(company);
    }

    @Override
    public CompanyInfo getNewestCompanyInfoItemById(Integer companyId) {
        if (companyId == null)
            return null;
        return companyRepository.findCompanyInfoById(companyId.longValue());
    }

    @Override
    public CompanyStatusItem getCompanyStatusItemById(Integer companyId) {
        if (companyId == null)
            return null;
        return companyRepository.getCompanyStatusItemById(companyId);
    }
}
