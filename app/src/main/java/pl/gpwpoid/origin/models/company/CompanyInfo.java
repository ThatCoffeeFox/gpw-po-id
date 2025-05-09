package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.keys.CompanyInfoId;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;

@Entity
@Table(name = "companies_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfo {
    @EmbeddedId
    private CompanyInfoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    private pl.gpwpoid.origin.models.company.Company company;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Pattern(regexp = "[A-Z]{3}") // Walidacja na poziomie Java
    @Column(name = "code", nullable = false, length = 3) // usunięto unique, zgodnie z SQL
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "town_id", referencedColumnName = "town_id", nullable = false),
            @JoinColumn(name = "postal_code", referencedColumnName = "postal_code", nullable = false)
    })
    private PostalCodesTowns postalCodesTowns;

    @Column(name = "street", length = 128)
    private String street;

    @Column(name = "street_number", length = 8)
    private String streetNumber;

    @Column(name = "apartment_number", length = 8)
    private String apartmentNumber;
}