package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.address.PostalCode;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface AddressService {
    List<PostalCodesTowns> getPostalCodesTowns();

    Optional<PostalCodesTowns> getPostalCodesTowns(Town town, String postalCode);

    List<PostalCodesTowns> getPostalCodesFromTown(Town town);

    void savePostalCodesTowns(PostalCodesTowns postalCodesTowns);

    List<Town> getTowns();

    Stream<Town> getTownsByName(String name, int offset, int limit);

    int countTownsByName(String name);

    Optional<Town> getTownById(Integer id);

    PostalCode getPostalCode(String postalCode);
}
