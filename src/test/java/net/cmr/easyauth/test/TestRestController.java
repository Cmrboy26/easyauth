package net.cmr.easyauth.test;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.cmr.easyauth.controller.EARestController;
import net.cmr.easyauth.resolver.EasyAuth;

@RestController
@Profile("test")
public class TestRestController extends EARestController<TestLogin> {

    // hasRole('USER') will only succeed if the user has an ACCESS token, not just a REFRESH token
    // hasAuthority('ACCESS') or hasAuthority('REFRESH') can be used as needed.
    @PreAuthorize("hasRole('USER')")
    // Specify endpoint
    @GetMapping("/sendEmail")
    // If the user is logged in (hasAuthority('ACCESS') is true or hasRole([Any Role Here]) is true),
    // TestLogin will be the login associated with the ACCESS token, otherwise null
    public ResponseEntity<String> sendEmail(@EasyAuth TestLogin login) {
        boolean emailSent = getLoginService(TestLoginService.class).sendEmail(login);
        if (emailSent) {
            return ResponseEntity.ok("Email sent to "+login.getEmail());
        } else {
            return ResponseEntity.badRequest().body("Email not sent to "+login.getEmail());
        }
    }
}
