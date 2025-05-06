package pl.gpwpoid.origin.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "postal_codes")
public class PostalCode {

    @Id
    @Column(name = "postal_code", length = 6)
    private String postalCode;

    public PostalCode() {}

    public String getPostalCode() {
        return postalCode;
    }
}
