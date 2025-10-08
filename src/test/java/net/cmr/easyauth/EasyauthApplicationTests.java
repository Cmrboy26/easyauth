package net.cmr.easyauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.login.CredentialException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.cmr.easyauth.entity.EAAuthority;
import net.cmr.easyauth.restpojo.LoginResponse;
import net.cmr.easyauth.test.TestApplication;
import net.cmr.easyauth.test.TestLogin;
import net.cmr.easyauth.test.TestLoginService;
import net.cmr.easyauth.test.TestRepository;
import net.cmr.easyauth.test.TestRestController;
import net.cmr.easyauth.util.CookieUtil;
import net.cmr.easyauth.util.JwtUtil;
import net.cmr.easyauth.util.NonNullMap;
import net.cmr.easyauth.util.NonNullMap.NullValueException;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EasyAuth Application Tests")
@Transactional
class EasyAuthApplicationTests {

	@Value("${test.key}")
	private String test;

	@Autowired private TestRepository repository;
	@Autowired private TestRestController restController;
	@Autowired private TestLoginService loginService;

	@Autowired
    private MockMvc mockMvc;

	@BeforeAll
	@DisplayName("Set up JwtUtil secret key")
	static void setUpJwtUtil() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
		secretKeyField.setAccessible(true);
		secretKeyField.set(null, Keys.secretKeyFor(SignatureAlgorithm.HS256));
	}

	@Test
	@BeforeEach
	@DisplayName("Context loads and beans are injected")
	void contextLoads() {
		assertEquals(test, "value");
		assertNotNull(repository);
		assertNotNull(restController);
		assertNotNull(loginService);
	}

	@Test
	@DisplayName("Repository should read and write TestLogin objects correctly")
	void repositoryReadWrites() throws Exception {
		TestLogin login = createTestLogin();
		repository.save(login);
		Optional<TestLogin> readLogin = repository.findById(login.getId());
		assertTrue(readLogin.isPresent());
		TestLogin readLoginObject = readLogin.get();
		assertEquals(login.getId(), readLoginObject.getId());
		assertEquals(login.getUsername(), readLoginObject.getUsername());
		assertEquals(login.getPassword(), readLoginObject.getPassword());
	}

	@Test
	@DisplayName("JWT signs tokens properly")
	void jwtSignsProperly() throws Exception {
		TestLogin login = createTestLogin();
		setTestLoginId(login, 23502);
		String refreshToken = JwtUtil.signJwt(login, 1000, false);
		String accessToken = JwtUtil.signJwt(login, 1000, true);
		assertTrue(JwtUtil.isTokenType(refreshToken, false), "Refresh token signing and verifying incorrect");
		assertTrue(JwtUtil.isTokenType(accessToken, true), "Access token signing and verifying incorrect");
	}

	@Test
	@DisplayName("Get JWT from cookies")
	void testGettingJwtFromCookies() throws Exception {
		TestLogin login = createTestLogin();
		setTestLoginId(login, idIncrement);
		String accessToken = JwtUtil.signJwt(login, 1000, true);
		String refreshToken = JwtUtil.signJwt(login, 1000, false); // Changed variable name for clarity

		assertNotNull(accessToken);
		assertNotNull(refreshToken);

		// Create separate mock instances for each test case
		HttpServletRequest mockHttpWithTokens = mock(HttpServletRequest.class);
		HttpServletRequest mockHttpWithoutTokens = mock(HttpServletRequest.class);

		Cookie accessCookie = new Cookie("accessToken", accessToken);
		Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

		// Cookies with both access and refresh tokens
		Cookie[] cookiesWithTokens = new Cookie[] {
			new Cookie("TestCookie", "TestCookieValue"),
			new Cookie("Authorization", "Bearer " + accessToken),
			accessCookie,
			refreshCookie
		};

		// Cookies without the specific tokens (only the irrelevant ones)
		Cookie[] cookiesWithoutTokens = new Cookie[] {
			new Cookie("TestCookie", "TestCookieValue"),
			new Cookie("Authorization", "Bearer SomeOtherToken")
		};

		when(mockHttpWithTokens.getCookies()).thenReturn(cookiesWithTokens);
		when(mockHttpWithoutTokens.getCookies()).thenReturn(cookiesWithoutTokens);

		// Test finding tokens when they exist
		assertEquals(accessToken, CookieUtil.getJwtFromCookies(mockHttpWithTokens, true));
		assertEquals(refreshToken, CookieUtil.getJwtFromCookies(mockHttpWithTokens, false));

		// Test not finding tokens when they don't exist
		assertEquals(null, CookieUtil.getJwtFromCookies(mockHttpWithoutTokens, true));
		assertEquals(null, CookieUtil.getJwtFromCookies(mockHttpWithoutTokens, false));
	}

	@Test
	@DisplayName("Cascade operations with authorities")
	void cascadeOperationsTest() throws Exception {
		TestLogin login = createTestLogin();
		EAAuthority authority = new EAAuthority("CASCADE_TEST");
		
		login.addAuthority(authority);
		repository.save(login);
		
		// Both login and authority should be saved due to cascade
		Optional<TestLogin> savedLogin = repository.findById(login.getId());
		assertTrue(savedLogin.isPresent());
		assertTrue(savedLogin.get().hasAuthority("CASCADE_TEST"));
	}

	@Test
	@DisplayName("NonNullMap throws NullValueException when null value is retrieved")
	void nonNullMapFunctionalityTest() {
		Map<String, String> map = Map.of("username", "joshua123", "password", "insecurePassword123");
		NonNullMap<String, String> nonNullMap = new NonNullMap<>(map);
		assertEquals(nonNullMap.get("username"), map.get("username"));
		assertEquals(nonNullMap.get("password"), map.get("password"));
		assertEquals(nonNullMap.getNullable("username"), map.get("username"));
		assertEquals(nonNullMap.getNullable("email"), map.get("email"));

		try {
			String email = nonNullMap.get("email");
			throw new AssertionError("NonNullMap.get(missing entry key) should throw NullValueException");
		} catch (NullValueException e) {
			// Proper implementation
		}
	}

	@Test
	@DisplayName("Registration adds user to database")
	void registrationAddsTest() {
		Map<String, String> map = Map.of("username", "IAmUnique123", "password", "password1234", "name", "Theodore");
		NonNullMap<String, String> nonNullMap = new NonNullMap<>(map);
		TestLogin login = loginService.registerUser(nonNullMap);
		assertEquals(repository.findById(login.getId()).get().getUsername(), login.getUsername());
	}

	@Test
	@DisplayName("Registration fails when unique column value already exists in database")
	void registrationUniqueConstraintsFailTest() {
		Map<String, String> map1 = Map.of("username", "MyUsernameONLY", "password", "password4567", "name", "John");
		NonNullMap<String, String> nonNullMap1 = new NonNullMap<>(map1);
		Map<String, String> map2 = Map.of("username", "MyUsernameONLY", "password", "differentPassword", "email", "george@gmail.com", "name", "George");
		NonNullMap<String, String> nonNullMap2 = new NonNullMap<>(map2);
		
		loginService.registerUser(nonNullMap1);
		assertThrows(IllegalArgumentException.class, () -> {
			loginService.registerUser(nonNullMap2);
		}, "IllegalArgumentException should have been thrown from 'username' unique constraint violation");
	}

	@Test
	@DisplayName("Registration fails when missing necessary value")
	void registrationMissingRequiredConstraintFailTest() {
		Map<String, String> map1 = Map.of("username", "MyUsernameONLY", "password", "password4567", "email", "helloWorld@gmail.com");
		NonNullMap<String, String> nonNullMap1 = new NonNullMap<>(map1);
		
		assertThrows(NullValueException.class, () -> {
			loginService.registerUser(nonNullMap1);
		}, "NullValueException should have been thrown from null value in 'name' column, which is required");
	}

	@Test
	@DisplayName("Login succeeds given the proper credentials")
	void loginSucceedsAsExpectedTest() throws CredentialException {
		Map<String, String> credentials = Map.of("username", "johnFive67", "password", "myAwesomePassword123", "email", "john567@gmail.com", "name", "John Awesome");
		NonNullMap<String, String> credentialsMap = new NonNullMap<>(credentials);
		loginService.registerUser(credentialsMap);
		LoginResponse response = loginService.loginUser(credentialsMap);
		assertNotNull(response);
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getRefreshToken());
	}

	@Test
	@DisplayName("Login fails without proper credentials")
	void loginFailsAsExpectedTest() {
		Map<String, String> credentials = Map.of("username", "johnWrongzo", "password", "myAwesomePassword123", "email", "johnwrongzo@gmail.com", "name", "John Wrongzo");
		NonNullMap<String, String> credentialsMap = new NonNullMap<>(credentials);
		loginService.registerUser(credentialsMap);

		NonNullMap<String, String> wrongUsernameMap = new NonNullMap<>(Map.of("username", "johnCorrecto", "password", "myAwesomePassword123"));
		NonNullMap<String, String> wrongPasswordMap = new NonNullMap<>(Map.of("username", "johnWrongzo", "password", "myIncorrectPassword"));
		assertThrows(CredentialException.class, () -> {
			loginService.loginUser(wrongUsernameMap);
		}, "Incorrect username field should have failed login, but succeeded");
		assertThrows(CredentialException.class, () -> {
			loginService.loginUser(wrongPasswordMap);
		}, "Incorrect password field should have failed login, but succeeded");
	}

	// Endpoint Testing
	@Test
	@DisplayName("Login endpoints are reachable")
	void testLoginEndpoints() throws Exception {
		mockMvc.perform(post("/auth/register"))
			.andExpect(status().is4xxClientError());

		mockMvc.perform(post("/auth/login"))
			.andExpect(status().is4xxClientError());

		mockMvc.perform(get("/auth/refresh"))
			.andExpect(status().is4xxClientError());

		mockMvc.perform(get("/auth/authorities"))
			.andExpect(status().is4xxClientError());
	}

	@Test
	@DisplayName("Admin endpoints are reachable")
	void testAdminEndpoints() throws Exception {
		mockMvc.perform(post("/auth/admin/user"))
			.andExpect(status().isForbidden());
	}


	// Helper methods

	@DisplayName("Set TestLogin id using reflection")
	private void setTestLoginId(TestLogin login, long id) throws Exception {
		Field idField = login.getClass().getSuperclass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(login, id);
	}

	private static int idIncrement;

	private TestLogin createTestLogin() throws Exception {
		NonNullMap<String, String> registerParams = new NonNullMap<>(Map.of("username", "username"+(++idIncrement), "password", "password"+idIncrement, "name", "name"+idIncrement));
		TestLogin login = loginService.createLogin(registerParams);
		return login;
	}

	@AfterEach
	@DisplayName("Clean up repository after each test")
	void cleanUp() {
		repository.deleteAll();
	}

}
