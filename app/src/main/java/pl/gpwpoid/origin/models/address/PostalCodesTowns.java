package pl.gpwpoid.origin.models.address;

import jakarta.persistence.*;
import lombok.*;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.models.keys.PostalCodesTownsId;
import pl.gpwpoid.origin.models.address.Town;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "postal_codes_towns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodesTowns {
    @EmbeddedId
    private PostalCodesTownsId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("postalCode")
    @JoinColumn(name = "postal_code")
    private pl.gpwpoid.origin.models.address.PostalCode postalCode;


    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("townId")
    @JoinColumn(name = "town_id")
    private Town town;

    // Relacje zwrotne, jeśli potrzebne (np. w AccountInfo, CompanyInfo)
     @OneToMany(mappedBy = "postalCodesTowns")
     private Set<AccountInfo> accountInfos;

     @OneToMany(mappedBy = "postalCodesTowns")
     private Set<CompanyInfo> companyInfos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostalCodesTowns that = (PostalCodesTowns) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PostalCodesTowns{" +
                "id=" + id +
                '}';
    }

}