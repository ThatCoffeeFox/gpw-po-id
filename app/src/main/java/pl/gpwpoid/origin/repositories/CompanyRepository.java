package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {

}
