package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.models.company.CompanyStatus;
import pl.gpwpoid.origin.models.keys.CompanyInfoId;
import pl.gpwpoid.origin.ui.views.DTO.CompanyUpdateDTO;

import java.util.Date;
import java.util.HashSet;

@Component
public class CompanyFactory {
    private final CompanyStatusFactory companyStatusFactory;

    public CompanyFactory(CompanyStatusFactory companyStatusFactory) {
        this.companyStatusFactory = companyStatusFactory;
    }

    public Company createCompany(
            String companyName,
            String companyCode,
            String street,
            String streetNumber,
            String apartmentNumber,
            PostalCodesTowns postalCodesTowns
    ) {
        if (companyName == null)
            throw new IllegalArgumentException("Company name cannot be null");
        if(companyCode == null)
            throw new IllegalArgumentException("Company code cannot be null");
        if(postalCodesTowns == null)
            throw new IllegalArgumentException("PostalCodesTowns cannot be null");

        Company company = new Company();
        company.setCompanyInfos(new HashSet<>());
        company.setIpos(new HashSet<>());
        company.setCompanyStatuses(new HashSet<>());

        CompanyInfo companyInfo = new CompanyInfo();

        CompanyInfoId companyInfoId = new CompanyInfoId();
        companyInfoId.setUpdatedAt(new Date());
        companyInfo.setId(companyInfoId);

        companyInfo.setName(companyName);
        companyInfo.setCode(companyCode);
        companyInfo.setStreet(street);
        companyInfo.setStreetNumber(streetNumber);
        companyInfo.setApartmentNumber(apartmentNumber);
        companyInfo.setPostalCodesTowns(postalCodesTowns);
        companyInfo.setCompany(company);

        company.getCompanyInfos().add(companyInfo);

        CompanyStatus companyStatus = companyStatusFactory.createCompanyStatus(company, false);

        company.getCompanyStatuses().add(companyStatus);

        return company;
    }

    public CompanyInfo createCompanyInfo(CompanyUpdateDTO companyUpdateDTO, PostalCodesTowns postalCodesTowns) {
        if(companyUpdateDTO == null)
            throw new IllegalArgumentException("Company update DTO cannot be null");
        if(companyUpdateDTO.getCompanyId() == null)
            throw new IllegalArgumentException("Company id cannot be null");
        if(companyUpdateDTO.getCompanyName() == null)
            throw new IllegalArgumentException("Company name cannot be null");
        if(companyUpdateDTO.getCompanyCode() == null)
            throw new IllegalArgumentException("Company code cannot be null");
        if(companyUpdateDTO.getTownId() == null || companyUpdateDTO.getPostalCode() == null)
            throw new IllegalArgumentException("Town id cannot be null");

        CompanyInfo companyInfo = new CompanyInfo();

        CompanyInfoId companyInfoId = new CompanyInfoId();
        companyInfoId.setUpdatedAt(new Date());
        companyInfo.setId(companyInfoId);

        companyInfo.setName(companyUpdateDTO.getCompanyName());
        companyInfo.setCode(companyUpdateDTO.getCompanyCode());
        companyInfo.setStreet(companyUpdateDTO.getStreet());
        companyInfo.setStreetNumber(companyUpdateDTO.getStreetNumber());
        companyInfo.setApartmentNumber(companyUpdateDTO.getApartmentNumber());
        companyInfo.setPostalCodesTowns(postalCodesTowns);

        return companyInfo;
    }
}
