package pl.gpwpoid.origin.company;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;

@Embeddable
public class CompanyStatusId {
    @Column(name = "company_id")
    private String companyId;

    @Column(name = "date")
    private OffsetDateTime date;

    public CompanyStatusId() {}
    public CompanyStatusId(String companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyStatusId that = (CompanyStatusId) o;
        return companyId.equals(that.companyId) && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(companyId, date);
    }

    public String getCompanyId() {
        return companyId;
    }

    public OffsetDateTime getDate() {
        return date;
    }
}
