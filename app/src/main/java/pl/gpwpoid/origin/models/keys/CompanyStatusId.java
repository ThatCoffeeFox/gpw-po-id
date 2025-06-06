package pl.gpwpoid.origin.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyStatusId implements Serializable {
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "date", columnDefinition = "TIMESTAMP DEFAULT current_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyStatusId that = (CompanyStatusId) o;
        return Objects.equals(companyId, that.companyId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, date);
    }
}
