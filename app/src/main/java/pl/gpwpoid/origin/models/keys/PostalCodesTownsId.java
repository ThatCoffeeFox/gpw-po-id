package pl.gpwpoid.origin.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostalCodesTownsId implements Serializable {
    @Column(name = "postal_code", length = 6)
    private String postalCode;

    @Column(name = "town_id")
    private Integer townId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostalCodesTownsId that = (PostalCodesTownsId) o;
        return Objects.equals(postalCode, that.postalCode) && Objects.equals(townId, that.townId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode, townId);
    }
}