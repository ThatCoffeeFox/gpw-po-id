package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.gpwpoid.origin.models.keys.CompanyStatusId;

@Entity
@Table(name = "companies_status")
@Data
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
}
