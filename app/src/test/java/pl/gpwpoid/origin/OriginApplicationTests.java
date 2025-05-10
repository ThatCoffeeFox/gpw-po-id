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
		System.out.println(passwordEncoder.encode("funadmin"));
		System.out.println(passwordEncoder.encode("funuser1"));
		System.out.println(passwordEncoder.encode("funuser2"));
		System.out.println(passwordEncoder.encode("funuser3"));
		System.out.println(passwordEncoder.encode("funuser4"));
	}

	@Test
	void checkPostalCodeQuery() {
		PostalCodeTownRepository postalCodeTownRepository;
	}

	@Test
	void contextLoads() {
	}

}
