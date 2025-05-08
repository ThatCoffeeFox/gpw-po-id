package pl.gpwpoid.origin.company;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    private Set<CompanyInfo> companyInfos;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER)
    private Set<CompanyStatus>  companyStatuses;

    @OneToMany(mappedBy = "company",  fetch = FetchType.EAGER)
    private Set<IPO> ipos;

    public Long getId() {
        return id;
    }

    public Set<CompanyInfo> getCompanyInfos() {
        return companyInfos;
    }

    public void setCompanyInfos(Set<CompanyInfo> companyInfos) {
        this.companyInfos = companyInfos;
    }

    public Set<CompanyStatus> getCompanyStatuses() {
        return companyStatuses;
    }

    public void setCompanyStatuses(Set<CompanyStatus> companyStatuses) {
        this.companyStatuses = companyStatuses;
    }

    public Set<IPO> getIpos() {
        return ipos;
    }

    public void setIpos(Set<IPO> ipos) {
        this.ipos = ipos;
    }
}
