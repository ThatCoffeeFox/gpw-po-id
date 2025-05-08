package pl.gpwpoid.origin.company;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Embeddable
public class CompanyInfoId implements Serializable {

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public CompanyInfoId() {}
    public CompanyInfoId(String companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyInfoId that = (CompanyInfoId) o;
        return companyId.equals(that.companyId) && updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(companyId, updatedAt);
    }

    public String getCompanyId() {
        return companyId;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
