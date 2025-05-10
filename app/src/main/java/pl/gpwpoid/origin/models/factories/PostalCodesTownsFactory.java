package pl.gpwpoid.origin.models.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.models.address.PostalCode;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.models.keys.PostalCodesTownsId;
import pl.gpwpoid.origin.repositories.PostalCodeTownRepository;
import pl.gpwpoid.origin.repositories.views.PostalCodesTownsItem;

import java.util.HashSet;

@Component
public class PostalCodesTownsFactory {

    private PostalCodeTownRepository postalCodeTownRepository;

    @Autowired
    public PostalCodesTownsFactory(PostalCodeTownRepository postalCodeTownRepository) {
        this.postalCodeTownRepository = postalCodeTownRepository;
    }

    public PostalCodesTowns create(PostalCode postalCode, Town town) {
        if(postalCode == null || town == null ||
                postalCode.getPostalCode().trim().isEmpty() || town.getTownId() == null) {
            throw new IllegalArgumentException("PostalCode lub Town nie mogą być null");
        }

        PostalCodesTownsId postalCodesTownsId = new PostalCodesTownsId();
        PostalCodesTownsItem combination = postalCodeTownRepository.findCombination(town.getTownId(), postalCode.getPostalCode());
        if(combination == null) {
            throw new IllegalArgumentException("Nieprawidłowa kombinacja adresowa");
        }

        postalCodesTownsId.setPostalCode(postalCode.getPostalCode());
        postalCodesTownsId.setTownId(town.getTownId());

        PostalCodesTowns newCombination = new PostalCodesTowns();
        newCombination.setId(postalCodesTownsId);
        newCombination.setTown(town);
        newCombination.setPostalCode(postalCode);

        town.getPostalCodesTowns().add(newCombination);
        postalCode.getPostalCodesTowns().add(newCombination);

        return newCombination;
    }
}
