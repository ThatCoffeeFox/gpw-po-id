package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.keys.CompanyStatusId;

import java.util.Objects;

@Entity
@Table(name = "companies_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatus {
    @EmbeddedId
    private CompanyStatusId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    private pl.gpwpoid.origin.models.company.Company company;

    @Column(name = "tradable", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean tradable;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyStatus that = (CompanyStatus) o;
        return Objects.equals(id,that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
