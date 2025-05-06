package pl.gpwpoid.origin.address;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostalCodeTownId implements Serializable {
    @Column(name = "postal_code", length = 6)
    private String postalCode;

    @Column(name = "town_id")
    private Integer townId;

    public PostalCodeTownId() {}
    public PostalCodeTownId(String postalCode, Integer townId) {
        this.postalCode = postalCode;
        this.townId = townId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostalCodeTownId that = (PostalCodeTownId) o;
        return Objects.equals(postalCode, that.postalCode) && Objects.equals(townId, that.townId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode, townId);
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Integer getTownId() {
        return townId;
    }
}