package pl.gpwpoid.origin.address;

import jakarta.persistence.*;

@Entity
@Table(name = "towns")
public class Town {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "town_id")
    private Integer townId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    public Town() {}

    public Integer getTownId() {
        return townId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
