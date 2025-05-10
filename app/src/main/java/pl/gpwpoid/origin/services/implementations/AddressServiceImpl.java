package pl.gpwpoid.origin.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.gpwpoid.origin.models.address.PostalCode;
import pl.gpwpoid.origin.models.address.PostalCodesTowns;
import pl.gpwpoid.origin.models.address.Town;
import pl.gpwpoid.origin.repositories.PostalCodeTownRepository;
import pl.gpwpoid.origin.services.AddressService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AddressServiceImpl implements AddressService {

    private final PostalCodeTownRepository postalCodeTownRepository;

    @Autowired
    public AddressServiceImpl(PostalCodeTownRepository postalCodeTownsRepository) {
        this.postalCodeTownRepository = postalCodeTownsRepository;
    }

    @Override
    public List<PostalCodesTowns> getPostalCodesTowns() {
        return postalCodeTownRepository.findAll();
    }

    @Override
    public Optional<PostalCodesTowns> getPostalCodesTowns(Town town, String postalCode) {
        PostalCode code = postalCodeTownRepository.findPostalCode(postalCode);
        return postalCodeTownRepository.findByTownAndPostalCode(town, code);
    }

    @Override
    public void savePostalCodesTowns(PostalCodesTowns postalCodesTowns) {
        postalCodeTownRepository.save(postalCodesTowns);
    }

    @Override
    public List<PostalCodesTowns> getPostalCodesFromTown(Town town) {
        return postalCodeTownRepository.findPostalCodesByTown(town);
    }

    @Override
    public List<Town> getTowns() {
        return postalCodeTownRepository.findAllTowns();
    }

    @Override
    public Stream<Town> getTownsByName(String name, int offset, int limit) {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<Town> result = postalCodeTownRepository.findTownsByNameContaining(name, pageable);
        return result.getContent().stream();
    }

    @Override
    public int countTownsByName(String name) {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Town> result = postalCodeTownRepository.findTownsByNameContaining(name, pageable);
        return (int) result.getTotalElements();
    }

    @Override
    public Optional<Town> getTownById(Integer id) {
        return postalCodeTownRepository.findTownById(id);
    }

    @Override
    public PostalCode getPostalCode(String postalCode) {
        return postalCodeTownRepository.findPostalCode(postalCode);
    }
}
