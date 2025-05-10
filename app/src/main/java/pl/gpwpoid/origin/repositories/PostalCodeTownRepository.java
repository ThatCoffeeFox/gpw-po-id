package pl.gpwpoid.origin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.gpwpoid.origin.models.address.PostalCode;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.models.keys.PostalCodesTownsId;
import pl.gpwpoid.origin.repositories.views.PostalCodesTownsItem;

@Repository
public interface PostalCodeTownRepository extends JpaRepository<PostalCodesTowns, PostalCodesTownsId> {
    @Query("""
        SELECT new pl.gpwpoid.origin.repositories.views.PostalCodesTownsItem(
            pct.postalCode.postalCode,
            pct.town.townId
        )
        FROM PostalCodesTowns pct
        WHERE pct.postalCode = :postalCode AND pct.town = :town
    """)
    PostalCodesTownsItem findCombination(Integer townId, String postalCode);

    @Query("""
        SELECT pc
        FROM PostalCode pc
        WHERE pc.postalCode = :postalCodeVal
    """)
    PostalCode getPostalCode(String postalCodeVal);

    @Query("""
        SELECT t
        FROM Town t
        WHERE t.townId = :townId
    """)
    Town getTown(Integer townId);

}
