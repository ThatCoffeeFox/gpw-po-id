package pl.gpwpoid.origin.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostalCodeTownRepository extends JpaRepository<PostalCodeTown,Long> {
}
