package net.cmr.easyauth.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import net.cmr.easyauth.entity.AdminRequest;
import net.cmr.easyauth.entity.Login;
import net.cmr.easyauth.entity.LoginRequest;
import net.cmr.easyauth.entity.RegisterRequest;
import net.cmr.easyauth.entity.SessionInfo;
import net.cmr.easyauth.repository.LoginRepository;
import net.cmr.easyauth.service.LoginService;

public abstract class AbstractAuthenticationController {

    @Autowired private LoginService loginService;
    @Autowired private LoginRepository loginRepository;

    @PostMapping("/auth/register")
    public CompletableFuture<ResponseEntity<String>> register(@RequestBody(required = true) RegisterRequest registerRequest) {
        return loginService.registerLogin(registerRequest);
    }

    @PostMapping("/auth/login")
    public CompletableFuture<ResponseEntity<SessionInfo>> login(HttpServletResponse response, @RequestBody(required = true) LoginRequest loginRequest) {
        return loginService.attemptLogin(response, loginRequest);
    }

    @PostMapping("/auth/verify")
    public CompletableFuture<ResponseEntity<String>> verify(@RequestBody(required = true) SessionInfo info) {
        return loginService.verifyLogin(info);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/auth/verify") //@RequestHeader(name = "Authorization", required = true) String jwtHeader
    public CompletableFuture<ResponseEntity<String>> verify(@AuthContextualize AuthContext authContext) throws Exception {
        String parsedToken = authContext.getJwt();
        return loginService.verifyLogin(parsedToken);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/auth/role")
    public ResponseEntity<String> getRole(@AuthContextualize AuthContext authContext) throws Exception {
        return ResponseEntity.ok(authContext.getLogin().getRole());
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/auth/refresh")
    public void refresh() {
        // TODO: best practice is having a long-lived token that can be used to generate short-lived tokens. refresh gives the short-lived token
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auth/admin/all")
    public List<Login> listAll(
            @RequestParam(required = false, defaultValue = "0", name = "page") int pageNumber,
            @RequestParam(required = false, defaultValue = "10", name = "entries") int entries) {
        return loginRepository.getView(pageNumber * entries, (pageNumber + 1) * entries - 1);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/admin/elevate")
    public CompletableFuture<ResponseEntity<String>> elevateUser(@RequestBody AdminRequest elevateRequest) {
        return loginService.elevateRole(elevateRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/admin/lock")
    public CompletableFuture<ResponseEntity<String>> lockUser(@RequestBody AdminRequest elevateRequest) {
        return loginService.lockUser(elevateRequest);
    }

}
