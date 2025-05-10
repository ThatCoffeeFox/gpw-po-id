package pl.gpwpoid.origin.models.address;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "postal_codes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostalCode {
    @Id
    @Column(name = "postal_code", length = 6)
    @Pattern(regexp = "[0-9]{2}-[0-9]{3}")
    private String postalCode;

    @OneToMany(mappedBy = "postalCode")
    private Set<PostalCodesTowns> postalCodesTowns;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostalCode that = (PostalCode) o;
        return Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postalCode);
    }

    @Override
    public String toString() {
        return "PostalCode{" +
                "postalCode='" + postalCode + '\'' +
                '}';
    }
}
