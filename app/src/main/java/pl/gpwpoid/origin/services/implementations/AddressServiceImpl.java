package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.factories.PostalCodesTownsFactory;
import pl.gpwpoid.origin.repositories.PostalCodeTownRepository;
import pl.gpwpoid.origin.services.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

    private PostalCodeTownRepository postalCodeTownRepository;
    private PostalCodesTownsFactory postalCodesTownsFactory;

    @Autowired
    public AddressServiceImpl(PostalCodeTownRepository postalCodeTownsRepository, PostalCodesTownsFactory postalCodesTownsFactory) {
        this.postalCodeTownRepository = postalCodeTownsRepository;
        this.postalCodesTownsFactory = postalCodesTownsFactory;
    }

    @Override
    public PostalCodesTowns getPostalCodesTowns(Integer townId, String postalCode) {
        System.out.println(postalCodeTownRepository.getPostalCode(postalCode).toString());
        return postalCodesTownsFactory.create(postalCodeTownRepository.getPostalCode(postalCode), postalCodeTownRepository.getTown(townId));
    }
}
