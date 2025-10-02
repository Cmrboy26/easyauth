package net.cmr.easyauth.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.cmr.easyauth.controller.CookieUtils;
import net.cmr.easyauth.entity.Login;
import net.cmr.easyauth.repository.LoginRepository;
import net.cmr.easyauth.service.LoginService;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private LoginRepository loginRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = getJwtToken(request);
        
        if (jwt != null) {
            if (!LoginService.isActiveJwsToken(jwt, loginRepository)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired authorization token");
                return;
            }
            
            try {
                String combinedCredential = LoginService.getCombinedCredentialFromJwt(jwt);
                String username = Login.getUsernameFromCombinedCredential(combinedCredential);
                String email = Login.getEmailFromCombinedCredential(combinedCredential);
                
                // Find the user in the database
                Login login = loginRepository.findByUsernameOrEmail(username, email);
                if (login != null) {
                    ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + login.getRole()));
                    
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        login.getUsername(),
                        null,
                        authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }

    public static String getJwtToken(HttpServletRequest request) {
        String jwtHeader = request.getHeader("Authorization");
        try {
            jwtHeader = LoginService.extractJwt(jwtHeader);
        } catch (IllegalArgumentException e) {
            jwtHeader = null;
        }
        String jwtCookie = CookieUtils.getJwtFromCookie(request);
        if (jwtHeader == null && jwtCookie == null) {
            return null;
        }
        if (jwtHeader == null ^ jwtCookie == null) {
            // only one is present
            return jwtHeader == null ? jwtCookie : jwtHeader;
        }
        // both are present
        if (!Objects.equals(jwtHeader, jwtCookie)) {
            // both are different for some reason??
            throw new IllegalArgumentException(String.format("Jwt header is not the same as Jwt cookie: %s != %s", jwtHeader, jwtCookie));
        }
        return jwtCookie;
    }
}
