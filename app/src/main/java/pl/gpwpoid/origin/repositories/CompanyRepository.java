package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    @Query(value = "SELECT * FROM tradable_companies()", nativeQuery = true)
    List<Integer> findTradableCompaniesId();

    @Query(value = """
                SELECT 
                    c.company_id,
                    ci.name,
                    ci.code,
                    t.name,
                    ci.postal_code,
                    ci.street,
                    ci.street_number,
                    ci.apartment_number,
                    shares_value(c.company_id),
                    shares_value_last_day(c.company_id)
                FROM companies c JOIN companies_info ci ON c.company_id = ci.company_id
                JOIN towns t ON ci.town_id = t.town_id
                WHERE ci.updated_at = (SELECT cii.updated_at 
                                    FROM companies_info cii 
                                    WHERE cii.company_id = c.company_id 
                                    ORDER BY cii.updated_at DESC 
                                    LIMIT 1)
            """, nativeQuery = true)
    List<CompanyListItem> getCompaniesAsViewItems();

    @Query(value = """
                SELECT
                    c.company_id,
                    ci.name,
                    ci.code,
                    t.name,
                    ci.postal_code,
                    ci.street,
                    ci.street_number,
                    ci.apartment_number,
                    shares_value(:companyId),
                    shares_value_last_day(:companyId)
                FROM companies c JOIN companies_info ci ON c.company_id = ci.company_id
                JOIN towns t ON ci.town_id = t.town_id
                WHERE c.company_id = :companyId
                AND ci.updated_at = (SELECT cii.updated_at
                                    FROM companies_info cii
                                    WHERE cii.company_id = c.company_id
                                    ORDER BY cii.updated_at DESC
                                    LIMIT 1)
            """, nativeQuery = true)
    CompanyListItem getCompanyById(Integer companyId);
}
