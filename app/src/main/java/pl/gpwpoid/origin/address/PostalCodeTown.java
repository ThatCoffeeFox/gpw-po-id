package pl.gpwpoid.origin.address;

import jakarta.persistence.*;

@Entity
@Table(name = "postal_codes_towns")
public class PostalCodeTown {
    @EmbeddedId
    private PostalCodeTownId id;

    @MapsId("postalCode")
    @ManyToOne(optional = false)
    @JoinColumn(name = "postal_code", nullable = false)
    private PostalCode postalCode;

    @MapsId("townId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "town_id", nullable = false)
    private Town town;


    public PostalCodeTown() {}
    public PostalCodeTown(PostalCodeTownId id) {
        this.id = id;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public Town getTown() {
        return town;
    }

    public PostalCodeTownId getId() {
        return id;
    }
}
