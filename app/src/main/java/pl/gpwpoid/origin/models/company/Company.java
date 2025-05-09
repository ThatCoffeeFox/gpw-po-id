package pl.gpwpoid.origin.models.company;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.company.CompanyInfo;
import pl.gpwpoid.origin.models.company.CompanyStatus;
import pl.gpwpoid.origin.models.company.IPO;
import pl.gpwpoid.origin.models.order.Order;

import java.util.Set;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompanyInfo> companyInfos;

    @OneToMany(mappedBy = "company")
    private Set<IPO> ipos;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompanyStatus> companyStatuses;

    @OneToMany(mappedBy = "company")
    private Set<Order> orders;
}
