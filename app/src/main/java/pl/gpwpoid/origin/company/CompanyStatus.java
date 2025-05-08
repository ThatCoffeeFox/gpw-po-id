package pl.gpwpoid.origin.company;

import jakarta.persistence.*;

@Entity
@Table(name = "companies_status")
public class CompanyStatus {

    @EmbeddedId
    private CompanyStatusId id;

    @MapsId("companyId")
    @JoinColumn(name = "company_id", nullable = false)
    @ManyToOne
    private Company company;

    @Column(name = "tradable", nullable = false)
    private Boolean tradable;

    public Boolean getTradable() {
        return tradable;
    }

    public void setTradable(Boolean tradable) {
        this.tradable = tradable;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanyStatusId getId() {
        return id;
    }
}
