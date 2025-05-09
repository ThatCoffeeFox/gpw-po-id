package pl.gpwpoid.origin.models.address;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.account.AccountInfo;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.models.keys.PostalCodesTownsId;
import pl.gpwpoid.origin.models.address.Town;

import java.util.Set;

@Entity
@Table(name = "postal_codes_towns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodesTowns {
    @EmbeddedId
    private PostalCodesTownsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postalCode")
    @JoinColumn(name = "postal_code")
    private pl.gpwpoid.origin.models.address.PostalCode postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("townId")
    @JoinColumn(name = "town_id")
    private Town town;

    // Relacje zwrotne, jeśli potrzebne (np. w AccountInfo, CompanyInfo)
     @OneToMany(mappedBy = "postalCodesTowns")
     private Set<AccountInfo> accountInfos;

     @OneToMany(mappedBy = "postalCodesTowns")
     private Set<CompanyInfo> companyInfos;
}