package pl.gpwpoid.origin.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.address.PostalCode;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.models.keys.PostalCodesTownsId;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostalCodeTownRepository extends JpaRepository<PostalCodesTowns, PostalCodesTownsId> {
    List<PostalCodesTowns> findPostalCodesByTown(Town town);

    List<PostalCodesTowns> findTownsByPostalCode(PostalCode postalCode);

    @Query(value = """
                SELECT t from Town t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%',:name,'%'))
            """, countQuery = """
                SELECT count(t) FROM Town t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<Town> findTownsByNameContaining(String name, Pageable pageable);

    @Query("""
                SELECT t from Town t
            """)
    List<Town> findAllTowns();

    @Query("""
                SELECT t from Town t WHERE t.townId = :id ORDER By 1 LIMIT 1
            """)
    Optional<Town> findTownById(Integer id);

    @Query("""
                SELECT pc FROM PostalCode pc WHERE pc.postalCode = :postalCode ORDER BY 1 LIMIT 1
            """)
    PostalCode findPostalCode(String postalCode);

    Optional<PostalCodesTowns> findByTownAndPostalCode(Town town, PostalCode postalCode);
}
