package pl.gpwpoid.origin.models.address;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;

import java.util.Set;

@Entity
@Table(name = "postal_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostalCode {
    @Id
    @Column(name = "postal_code", length = 6)
    @Pattern(regexp = "[0-9]{2}-[0-9]{3}")
    private String postalCode;

    @OneToMany(mappedBy = "postalCode")
    private Set<PostalCodesTowns> postalCodesTowns;
}
