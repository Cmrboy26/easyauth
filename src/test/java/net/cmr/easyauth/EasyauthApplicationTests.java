package net.cmr.easyauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.cmr.easyauth.test.TestApplication;
import net.cmr.easyauth.test.TestLogin;
import net.cmr.easyauth.test.TestLoginService;
import net.cmr.easyauth.test.TestRepository;
import net.cmr.easyauth.test.TestRestController;
import net.cmr.easyauth.util.CookieUtil;
import net.cmr.easyauth.util.JwtUtil;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class EasyAuthApplicationTests {

	@Value("${test.key}")
	private String test;

	@Autowired private TestRepository repository;
	@Autowired private TestRestController restController;
	@Autowired private TestLoginService loginService;

	@BeforeAll
	static void setUpJwtUtil() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
		secretKeyField.setAccessible(true);
		secretKeyField.set(null, Keys.secretKeyFor(SignatureAlgorithm.HS256));
	}

	@Test
	@BeforeEach
	void contextLoads() {
		assertEquals(test, "value");
		assertNotNull(repository);
		assertNotNull(restController);
		assertNotNull(loginService);
	}

	@Test
	void repositoryReadWrites() {
		TestLogin login = new TestLogin("username123", "password123");
		repository.save(login);
		Optional<TestLogin> readLogin = repository.findById(login.getId());
		assertTrue(readLogin.isPresent());
		TestLogin readLoginObject = readLogin.get();
		assertEquals(login.getId(), readLoginObject.getId());
		assertEquals(login.getUsername(), readLoginObject.getUsername());
		assertEquals(login.getPassword(), readLoginObject.getPassword());
	}

	@Test
	void jwtSignsProperly() throws Exception {
		TestLogin login = new TestLogin("username123", "password123");
		setTestLoginId(login, 23502);
		String refreshToken = JwtUtil.signJwt(login, false);
		String accessToken = JwtUtil.signJwt(login, true);
		assertTrue(JwtUtil.isTokenType(refreshToken, false), "Refresh token signing and verifying incorrect");
		assertTrue(JwtUtil.isTokenType(accessToken, true), "Access token signing and verifying incorrect");
	}

	@Test
	void getUserFromJwt() throws Exception {
		TestLogin login = new TestLogin("username123", "password123");
		repository.save(login);
		String refreshToken = JwtUtil.signJwt(login, false);
		String accessToken = JwtUtil.signJwt(login, true);

		Optional<TestLogin> refreshOnly = loginService.getUserFromJwt(null, refreshToken);
		assertTrue(refreshOnly.isPresent(), "Retrieving login from the refresh token failed.");
		assertEquals(login, refreshOnly.get());

		Optional<TestLogin> accessOnly = loginService.getUserFromJwt(accessToken, null);
		assertTrue(accessOnly.isPresent(), "Retrieving login from the access token failed.");
		assertEquals(login, accessOnly.get());

		Optional<TestLogin> both = loginService.getUserFromJwt(accessToken, refreshToken);
		assertTrue(both.isPresent(), "Retrieving login from both tokens failed.");
		assertEquals(login, both.get());

		Optional<TestLogin> none = loginService.getUserFromJwt(null, null);
		assertTrue(none == null, "Retrieving login with no tokens should return null.");
	}

	@Test
	void testGettingJwtFromCookies() throws Exception {
		TestLogin login = new TestLogin("username123", "password123");
		setTestLoginId(login, 234);
		String accessToken = JwtUtil.signJwt(login, true);
		String requestToken = JwtUtil.signJwt(login, false);

		HttpServletRequest mockHttp = mock(HttpServletRequest.class);
		Cookie accessCookie = new Cookie("jwt-token", accessToken);
		accessCookie.setAttribute("access", "true");
		Cookie requestCookie = new Cookie("jwt-token", requestToken);
		requestCookie.setAttribute("access", "false");

		Cookie[] cookieArray = new Cookie[] {
			new Cookie("TestCookie", "TestCookieValue"),
			new Cookie("Authorization", "Bearer "+accessToken),
			accessCookie
		};
		Cookie[] cookieArray2 = new Cookie[] {
			new Cookie("TestCookie", "TestCookieValue"),
			new Cookie("Authorization", "Bearer "+requestToken),
			requestCookie
		};

		when(mockHttp.getCookies())
			.thenReturn(cookieArray)
			.thenReturn(cookieArray2)
			.thenReturn(Arrays.copyOf(cookieArray, 2))
			.thenReturn(Arrays.copyOf(cookieArray2, 2));

		assertEquals(accessToken, CookieUtil.getJwtFromCookies(mockHttp, true));
		assertEquals(requestToken, CookieUtil.getJwtFromCookies(mockHttp, false));

		assertEquals(null, CookieUtil.getJwtFromCookies(mockHttp, true));
		assertEquals(null, CookieUtil.getJwtFromCookies(mockHttp, false));
	}

	// Helper methods
	private void setTestLoginId(TestLogin login, long id) throws Exception {
		Field idField = login.getClass().getSuperclass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(login, id);
	}

}
