package net.cmr.easyauth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.cmr.easyauth.controller.AbstractAuthenticationController;
import net.cmr.easyauth.repository.LoginRepository;
import net.cmr.easyauth.service.LoginService;

@SpringBootTest
class EasyauthApplicationTests {

	@Autowired protected AbstractAuthenticationController authenticationController;
	@Autowired protected LoginService loginService;
	@Autowired protected LoginRepository loginRepository;

	@Test
	@BeforeAll
	void contextLoads() {
		assertNotEquals(loginService, null);
		assertNotEquals(authenticationController, null);
		assertNotEquals(loginRepository, null);
	}

	@Test
	@BeforeAll
	void repositoryIsEmpty() {
		// Sanity check: repository is empty
		assertTrue(loginRepository.findAll().size() == 0);
	}

	@Test
	void authRegistrationValid() {
		
	}

}
