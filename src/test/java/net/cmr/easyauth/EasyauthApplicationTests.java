package net.cmr.easyauth;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RestController;

import net.cmr.easyauth.controller.EARestController;
import net.cmr.easyauth.entity.EALogin;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(Lifecycle.PER_CLASS)
class EasyAuthApplicationTests {

	@Test
	@BeforeAll
	void contextLoads() {

	}

	@Test
	void authRegistrationValid() {
		
	}

}
