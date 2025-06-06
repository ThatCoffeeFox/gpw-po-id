package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.company.CompanyStatus;

@Repository
public interface CompanyStatusRepository extends JpaRepository<CompanyStatus, Long> {
}
