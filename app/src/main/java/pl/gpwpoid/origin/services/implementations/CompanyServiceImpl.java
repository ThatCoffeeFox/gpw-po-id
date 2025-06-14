package pl.gpwpoid.origin.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import pl.gpwpoid.origin.services.OrderService;
import pl.gpwpoid.origin.ui.views.DTO.CompanyDTO;
import pl.gpwpoid.origin.ui.views.DTO.CompanyUpdateDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyStatusFactory companyStatusFactory;
    private final CompanyStatusRepository companyStatusRepository;
    private final AddressService addressService;
    private final CompanyFactory companyFactory;
    private final OrderService orderService;

    @Autowired
    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            CompanyStatusFactory companyStatusFactory,
            CompanyStatusRepository companyStatusRepository,
            AddressService addressService,
            CompanyFactory companyFactory,
            @Lazy OrderService orderService) {
        this.companyStatusFactory = companyStatusFactory;
        this.companyRepository = companyRepository;
        this.companyStatusRepository = companyStatusRepository;
        this.addressService = addressService;
        this.companyFactory = companyFactory;
        this.orderService = orderService;
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
    @Transactional
    public void setTradable(Integer companyId, Boolean tradable) {
        Company company = companyRepository.findById(Long.valueOf(companyId)).orElse(null);
        CompanyStatus companyStatus = companyStatusFactory.createCompanyStatus(company, tradable);
        companyStatusRepository.save(companyStatus);
        if (tradable)
            orderService.startOrderMatching(companyId);
        else
            orderService.stopOrderMatching(companyId);
    }

    @Override
    public Boolean isTradable(Integer companyId) {
        return companyRepository.isTradable(companyId);
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

    @Override
    @Transactional(readOnly = true)
    public List<CompanyListItem> getTop5MostValuableCompanies() {
        List<CompanyListItem> allCompanies = companyRepository.getCompaniesAsViewItems();

        return allCompanies.stream()
                .filter(item -> item.getCurrentSharePrice() != null)
                .sorted(Comparator.comparing(CompanyListItem::getCurrentSharePrice).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}
