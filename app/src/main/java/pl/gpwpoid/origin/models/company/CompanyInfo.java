package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.gpwpoid.origin.models.keys.CompanyInfoId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.keys.CompanyInfoId;

import java.util.Objects;

@Entity
@Table(name = "companies_info")
@Getter
@Setter
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

    @Pattern(regexp = "[A-Z]{3}")
    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @ManyToOne(fetch = FetchType.EAGER)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyInfo that = (CompanyInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}