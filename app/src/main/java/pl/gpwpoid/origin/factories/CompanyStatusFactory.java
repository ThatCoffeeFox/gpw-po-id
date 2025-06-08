package pl.gpwpoid.origin.factories;

import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.models.company.CompanyStatus;
import pl.gpwpoid.origin.models.keys.CompanyStatusId;

import java.util.Date;

@Component
public class CompanyStatusFactory {
    public CompanyStatus createCompanyStatus(
            Company company,
            Boolean tradable
    ) {
        if (company == null)
            throw new NullPointerException("Company is null");
        if (tradable == null)
            throw new NullPointerException("Tradable is null");

        CompanyStatusId companyStatusId = new CompanyStatusId();
        companyStatusId.setCompanyId(company.getCompanyId());
        companyStatusId.setDate(new Date());

        CompanyStatus companyStatus = new CompanyStatus();
        companyStatus.setId(companyStatusId);
        companyStatus.setCompany(company);
        companyStatus.setTradable(tradable);
        return companyStatus;
    }
}
