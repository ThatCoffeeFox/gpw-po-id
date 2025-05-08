package pl.gpwpoid.origin.company;


import jakarta.persistence.*;
import pl.gpwpoid.origin.address.PostalCodeTown;

@Entity
@Table(name = "companies_info")
public class CompanyInfo {

    @EmbeddedId
    private CompanyInfoId id;

    @MapsId("companyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(name = "postal_code", referencedColumnName = "postal_code"),
            @JoinColumn(name = "town_id", referencedColumnName = "town_id")
    })
    private PostalCodeTown postalCodeTown;

    @Column(name = "street", length = 128)
    private String street;

    @Column(name = "street_number", length = 8)
    private String streetNumber;

    @Column(name = "apartment_number", length = 8)
    private String apartmentNumber;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PostalCodeTown getPostalCodeTown() {
        return postalCodeTown;
    }

    public void setPostalCodeTown(PostalCodeTown postalCodeTown) {
        this.postalCodeTown = postalCodeTown;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public CompanyInfoId getInfoId() {
        return id;
    }
}
