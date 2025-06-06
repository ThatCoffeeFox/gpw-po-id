package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.Company;
import pl.gpwpoid.origin.repositories.views.AdminCompanyListItem;
import pl.gpwpoid.origin.repositories.views.CompanyListItem;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    @Query(value = "SELECT * FROM tradable_companies()", nativeQuery = true)
    List<Integer> findTradableCompaniesId();

    @Query(value = """
        SELECT 
            c.company_id,
            ci.name,
            ci.code
        FROM companies c JOIN companies_info ci ON c.company_id = ci.company_id
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
            ci.code
        FROM companies c JOIN companies_info ci ON c.company_id = ci.company_id
        WHERE c.company_id = :companyId
        AND ci.updated_at = (SELECT cii.updated_at
                            FROM companies_info cii
                            WHERE cii.company_id = c.company_id
                            ORDER BY cii.updated_at DESC
                            LIMIT 1)
    """, nativeQuery = true)
    CompanyListItem getCompanyById(Integer companyId);

    @Query(value = """
        SELECT 
            c.company_id AS companyId,
            ci.name AS companyName,
            ci.code AS companyCode,
            shares_value(c.company_id) AS currentPrice,
            shares_value_last_day(c.company_id) AS previousPrice,
            cs.tradable AS tradable
        FROM companies c
        JOIN companies_info ci ON c.company_id = ci.company_id
        JOIN companies_status cs ON c.company_id = cs.company_id
        WHERE ci.updated_at = (SELECT cii.updated_at
                            FROM companies_info cii
                            WHERE cii.company_id = c.company_id
                            ORDER BY cii.updated_at DESC
                            LIMIT 1)
        AND cs.date = (SELECT css.date
                        FROM companies_status css
                        WHERE css.company_id = c.company_id
                        ORDER BY css.date DESC
                        LIMIT 1)
    """, nativeQuery = true)
    List<AdminCompanyListItem> getAdminCompanyListItems();
}
