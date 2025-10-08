package net.cmr.easyauth.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.security.auth.login.CredentialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.NotSupportedException;
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.filter.JwtAuthenticationFilter;
import net.cmr.easyauth.resolver.EasyAuth;
import net.cmr.easyauth.restpojo.LoginResponse;
import net.cmr.easyauth.restpojo.RefreshResponse;
import net.cmr.easyauth.service.EALoginService;
import net.cmr.easyauth.util.CookieUtil;
import net.cmr.easyauth.util.NonNullMap;

@RestController
public abstract class EARestController<L extends EALogin> {
    
    @Autowired
    protected EALoginService<L> loginService;

    // Check if able to add, provides both REFRESH and ACCESS token as cookies, REFRESH token as body
    // Accessable by ANYONE
    @PostMapping("/auth/register")
    public void register(@RequestBody(required = true) Map<String, String> registerPost) {
        // TODO: Rate limiting, restrict register calls based on IP
        NonNullMap<String, String> nonNullMap = new NonNullMap<>(registerPost);
        loginService.registerUser(nonNullMap);
    }

    // Check credentials, provides both REFRESH and ACCESS token as cookies, REFRESH token as body
    // Accessable by ANYONE
    /**
     * 
     * @param loginPost request body with keys "username" and "password" by default
     * @throws CredentialException 
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(HttpServletResponse response, @RequestBody(required = true) Map<String, String> loginPost) throws CredentialException {
        // TODO: Rate limiting
        NonNullMap<String, String> nonNullMap = new NonNullMap<>(loginPost);
        LoginResponse loginResponse = loginService.loginUser(nonNullMap);
        ResponseEntity<LoginResponse> entity = ResponseEntity.ok(loginResponse);
        response.addCookie(CookieUtil.generateJwtCookie(loginResponse.getRefreshToken(), false));
        response.addCookie(CookieUtil.generateJwtCookie(loginResponse.getAccessToken(), true));
        return entity;
    }

    // Recieves REFRESH token, returns ACCESS token as body AND as cookie
    // Accessable by those with a REFRESH token
    @GetMapping("/auth/refresh")
    @PreAuthorize("hasAuthority('REFRESH')")
    public ResponseEntity<RefreshResponse> refresh(@RequestParam(name = "requestToken", required = false) String requestJwt, HttpServletResponse response, HttpServletRequest request) throws CredentialException, JwtException {
        // TODO: Rate limiting
        if (requestJwt == null) {
            requestJwt = JwtAuthenticationFilter.extractValidJwt(request, false);
        }
        RefreshResponse refreshResponse = loginService.refreshUser(requestJwt);
        ResponseEntity<RefreshResponse> entity = ResponseEntity.ok(refreshResponse);
        response.addCookie(CookieUtil.generateJwtCookie(refreshResponse.getAccessToken(), true));
        return entity;
    }

    // Recieves ACCESS token as header AND/OR cookie
    // Accessable by those with an ACCESS token
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/auth/authorities")
    public ResponseEntity<List<String>> authorities(@EasyAuth EALogin login) {
        List<String> authorities = loginService.getAuthorities(login);
        ResponseEntity<List<String>> entity = ResponseEntity.ok(authorities);
        return entity;
    }

    // Admin 
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auth/admin/user")
    public ResponseEntity<L> getUser(@RequestParam(required = false) Long id, @RequestParam(required = false) String username) {
        Optional<L> loginOptional = null;
        if (id != null) {
            loginOptional = loginService.getUser(id);
        }
        if (username != null && (loginOptional == null || loginOptional.isEmpty())) {
            loginOptional = loginService.getUserByUsername(username);
        }
        if (loginOptional == null || loginOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(loginOptional.get());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/admin/addRole")
    public ResponseEntity<String> addRole(@RequestBody(required = false) Map<String, String> postParams, @RequestParam(required = false) String role, @RequestParam(required = false) Long id, @RequestParam(required = false) String username) {
        // TODO: This checking is bad and doesn't work
        /*if (role == null) {
            if (postParams == null) {
                throw new IllegalArgumentException("Missing request body");
            }
            role = postParams.get("role");
            if (role == null) {
                throw new IllegalArgumentException("Missing valid role");
            }
        }
        role = "ROLE_" + role;
        Optional<L> login = null;
        if (id == null && postParams != null) {
            // Prioritize ID
            id = Long.valueOf(postParams.get("id"));
            login = loginService.getUser(id);
        }
        if (username == null && postParams != null && (login == null || login.isEmpty())) {
            username = postParams.get("username");
            login = loginService.getUserByUsername(username);
        }
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("Missing valid id or username");
        }*/

        //L loginObject = login.get();
        //loginService.addAuthority(loginObject, role, true); 
        //return ResponseEntity.ok("Role successfully added");
        throw new RuntimeException();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/admin/addAuthority")
    public ResponseEntity<String> addAuthority(@RequestBody(required = false) Map<String, String> postParams, @RequestParam(required = false) String authority, @RequestParam(required = false) Long id, @RequestParam(required = false) String username) {
        throw new RuntimeException();
        // TODO: This checking is bad and doesn't work
        /*if (authority == null) {
            if (postParams == null) {
                throw new IllegalArgumentException("Missing request body");
            }
            authority = postParams.get("authority");
            if (authority == null) {
                throw new IllegalArgumentException("Missing valid authority");
            }
        }
        Optional<L> login = null;
        if (id == null && postParams != null) {
            // Prioritize ID
            id = Long.valueOf(postParams.get("id"));
            login = loginService.getUser(id);
        }
        if (username == null && postParams != null && (login == null || login.isEmpty())) {
            username = postParams.get("username");
            login = loginService.getUserByUsername(username);
        }
        if (login == null || login.isEmpty()) {
            throw new IllegalArgumentException("Missing valid id or username");
        }*/

        //L loginObject = login.get();
        //loginService.addAuthority(loginObject, authority, true); 
        //return ResponseEntity.ok("Role successfully added");
    }
    
    // Helper Methods

    protected <T extends EALoginService<L>> T getLoginService(Class<T> loginServiceClass) {
        return loginServiceClass.cast(loginService);
    }

}
