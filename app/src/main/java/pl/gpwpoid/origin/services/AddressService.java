package pl.gpwpoid.origin.services;

import pl.gpwpoid.origin.models.address.PostalCodesTowns;

public interface AddressService {
    PostalCodesTowns getPostalCodesTowns(Integer townId, String postalCode);
}
