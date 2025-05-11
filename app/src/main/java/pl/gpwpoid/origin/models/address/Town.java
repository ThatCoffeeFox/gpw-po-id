package pl.gpwpoid.origin.models.address;

import jakarta.persistence.*;

import lombok.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "towns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Town {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "town_id")
    private Integer townId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @OneToMany(mappedBy = "town", fetch = FetchType.EAGER)
    private Set<PostalCodesTowns> postalCodesTowns;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Town town = (Town) o;
        return Objects.equals(townId, town.townId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(townId);
    }

    @Override
    public String toString() {
        return "Town{" +
                "townId=" + townId +
                ", name='" + name + '\'' +
                '}';
    }
}
