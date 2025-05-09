package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;

@Repository
public interface PostalCodeTownRepository extends JpaRepository<PostalCodesTowns,Long> {
}
