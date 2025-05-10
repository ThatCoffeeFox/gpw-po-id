package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.Company;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    @Query(value = "SELECT * FROM tradable_companies()", nativeQuery = true)
    List<Integer> findTradableCompaniesId();
}
