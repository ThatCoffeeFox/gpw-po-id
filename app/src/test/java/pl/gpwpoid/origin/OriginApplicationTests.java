package pl.gpwpoid.origin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.gpwpoid.origin.repositories.PostalCodeTownRepository;

@SpringBootTest
class OriginApplicationTests {

	@Test
	void checkPasswordEncryption() {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		System.out.println(passwordEncoder.encode("admin"));
	}

	@Test
	void checkPostalCodeQuery() {
		PostalCodeTownRepository postalCodeTownRepository;
	}

	@Test
	void contextLoads() {
	}

}
