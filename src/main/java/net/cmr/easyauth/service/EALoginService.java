package net.cmr.easyauth.service;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.CredentialException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.node.POJONode;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import net.cmr.easyauth.entity.EAAuthority;
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.respository.EAAuthorityRepository;
import net.cmr.easyauth.respository.EALoginRepository;
import net.cmr.easyauth.restpojo.LoginResponse;
import net.cmr.easyauth.restpojo.RefreshResponse;
import net.cmr.easyauth.util.JwtUtil;
import net.cmr.easyauth.util.NonNullMap;
import net.cmr.easyauth.util.NonNullMap.NullValueException;

public abstract class EALoginService<L extends EALogin> {

    @Autowired
    private EALoginRepository<L> loginRepository;
    @Autowired
    private EAAuthorityRepository authorityRepository;
    @Value("${cmr.easyauth.adminUsername}")
    private String adminUsername;

    public static final Logger logger = LoggerFactory.getLogger(EALoginService.class);

    protected abstract L createNewInstance(NonNullMap<String, String> registerParams);

    /**
     * @param validAccessJwt the access JWT of the desired user, may be null
     * @param validRefreshJwt the refresh JWT of the desired user, may be null
     * @return an optional if the database was pinged, null if both tokens are null.
     */
    @Transactional(readOnly = true)
    public Optional<L> getUserFromJwt(String validAccessJwt, String validRefreshJwt) {
        Long accessID = null;
        Long refreshID = null;
        if (validAccessJwt == null && validRefreshJwt == null) {
            return null;
        }
        Long targetId = null;
        if (validAccessJwt != null) {
            accessID = JwtUtil.getId(validAccessJwt);
            targetId = accessID;
        }
        if (validRefreshJwt != null) {
            refreshID = JwtUtil.getId(validRefreshJwt);
            targetId = refreshID;
        }
        // If both aren't the same, there's likely tampering, throw an error
        // Very very very unlikely I think
        if (accessID != null && refreshID != null && accessID != refreshID) {
            throw new InputMismatchException("Underlying IDs don't match.");
        }
        if (targetId == null) {
            throw new NullPointerException("Target Id is null, likely a programming error");
        }
        return loginRepository.findByIdWithAuthorities(targetId);
    }

    public L createLogin(NonNullMap<String, String> registerParams) {
        return createNewInstance(registerParams);
    }

    /**
     * @param registerParams a {@link NonNullMap}, which will throw an error when
     * a value retrieved from a key returns null.
     * @return a generated login from the register parameters
     * @throws NullValueException if the request was missing a necessary key-value pair
     * @throws IllegalArgumentException if unique constraints were violated (a certain unique value was already taken)
     */
    @Transactional
    @Async
    public L registerUser(NonNullMap<String, String> registerParams) {
        if (!areRequiredCredentialsUnique(registerParams)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (!overrideAreCredentialsUnique(registerParams)) {
            throw new IllegalArgumentException("Unique parameter(s) already taken");
        }
        L login = createLogin(registerParams);

        addAuthority(login, "ROLE_USER", false);
        if (adminUsername != null && registerParams.get("username").equals(adminUsername)) {
            addAuthority(login, "ROLE_ADMIN", false);
            logger.warn("Admin registered using adming username in application.properties. It is recommended that you remove this line in production.");
        }
        login = loginRepository.save(login);
        return login;
    }

    @Async
    public LoginResponse loginUser(NonNullMap<String, String> loginParams) throws CredentialException {
        Optional<L> login = loginRepository.findByUsername(loginParams.get("username"));
        if (login.isEmpty()) {
            throw new CredentialException("Username and/or password incorrect");
        }

        verifyCredentials(login.get(), loginParams);
        overrideVerifyCredentials(login.get(), loginParams);

        String refreshToken = JwtUtil.signJwt(login.get(), false);
        String accessToken = JwtUtil.signJwt(login.get(), true);
        LoginResponse lr = new LoginResponse(refreshToken, accessToken);
        return lr;
    }

    @Async
    public RefreshResponse refreshUser(@Nullable String refreshToken) throws JwtException, CredentialException {
        if (!JwtUtil.isTokenType(refreshToken, false)) {
            throw new JwtException("Invalid refresh token. Please log in.");
        }
        long id = JwtUtil.getId(refreshToken);
        // At this point, the refresh token is valid
        Optional<L> login = loginRepository.findById(id);
        if (login.isEmpty()) {
            throw new CredentialException("Account does not exist.");
        }
        String accessToken = JwtUtil.signJwt(login.get(), true);
        RefreshResponse refreshResponse = new RefreshResponse(accessToken);
        return refreshResponse;
    }

    @Async
    public List<String> getAuthorities(EALogin login) {
        return login.getAuthorities().stream().map(Object::toString).toList();
    }

    public Optional<L> getUser(Long id) {
        return loginRepository.findById(id);
    }
    public Optional<L> getUserByUsername(String username) {
        return loginRepository.findByUsername(username);
    }

    // Helper Methods

    private final boolean areRequiredCredentialsUnique(NonNullMap<String, String> registerParams) {
        return loginRepository.findByUsername(registerParams.get("username")).isEmpty();
    }

    /**
     * Can be overwritten to add additional checks to registering a user based on
     * registration parameters. 
     * 
     * For example, if an email is unique AND required, override this
     * and add the following (after adding a Optional<L> findByEmail() method in your repository class):
     <br><br><code>
        return getLoginRepository(ExampleRepository.class).findByEmail(registerParams.get("email")).isEmpty();
     </code><br><br>
     * If an email is unique and not necessarily required upon registration:
     <br><br><code>
        return registerParams.getNullable("email") == null || getLoginRepository(ExampleRepository.class).findByEmail(registerParams.get("email")).isEmpty();
     </code><br>
     * @param registerParams
     * @return true if parameters are valid, false if they conflict
     */
    public boolean overrideAreCredentialsUnique(NonNullMap<String, String> registerParams) {
        return true;
    }

    public void verifyCredentials(L login, NonNullMap<String, String> loginParams) throws CredentialException {
        if (!login.checkPassword(loginParams.get("password"))) {
            throw new CredentialException("Username and/or password incorrect");
        }
    } 

    /**
     * Can be overwritten to add additional verification steps to a login attempt.
     * @param login the {@link EALogin} object associated with the "username" field in loginParams
     * @param loginParams information passed in from the user
     * @throws CredentialException whenever a login is invalid, a CredentialException should be
     * thrown to prevent a successful login.
     * @throws AdditionalStepsRequiredException if additional steps are required to complete the
     * verification, this should be thrown with a message describing the user's next required
     * steps. This exception is expected to be used with multi-factor authentication like
     * email verification.
     */
    public void overrideVerifyCredentials(L login, NonNullMap<String, String> loginParams) throws CredentialException, AdditionalStepsRequiredException { 
        
    }

    /**
     * Adds an authority to a given user. NOTE: Changes MUST be saved via {@code loginRepository.save(login)}
     * AFTER this method is called to have changes persist OR save must be true
     * @param login
     * @param authority
     * @param save will save the login after the method is called
     */
    public void addAuthority(L login, String authority, boolean save) {
        EAAuthority userRole = getOrCreateAuthority(authority);
        login.addAuthority(userRole);
        loginRepository.save(login);
    }

    public EAAuthority getOrCreateAuthority(String authority) {
        Optional<EAAuthority> userAuthority = authorityRepository.findByAuthorityValue(authority);
        EAAuthority userAuthorityObject = null;
        if (userAuthority.isEmpty()) {
            userAuthorityObject = new EAAuthority(authority);
            authorityRepository.save(userAuthorityObject);
        } else {
            userAuthorityObject = userAuthority.get();
        }
        return userAuthorityObject;
    }

    protected <T extends EALoginRepository<L>> T getLoginRepository(Class<T> clazz) {
        return clazz.cast(loginRepository);
    }
    

}
