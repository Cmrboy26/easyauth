package net.cmr.easyauth.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.cmr.easyauth.entity.EAAuthority;
import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.service.EALoginService;
import net.cmr.easyauth.util.CookieUtil;
import net.cmr.easyauth.util.HeaderUtil;
import net.cmr.easyauth.util.JwtUtil;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final EALoginService<? extends EALogin> loginService;

    @Autowired
    public JwtAuthenticationFilter(EALoginService<? extends EALogin> loginService) {
        this.loginService = loginService;
    }

    private static boolean prioritizeHeaders;

    @Value("{cmr.easyauth.prioritizeHeaders:true}")
    private void setPrioritizeHeaders(String prioritizeHeaders) {
        JwtAuthenticationFilter.prioritizeHeaders = Boolean.valueOf(prioritizeHeaders);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (loginService != null) {
            // These tokens are validated
            try {
                String jwtRefreshToken = extractValidJwt(request, false);
                String jwtAccessToken = extractValidJwt(request, true);
                if (jwtAccessToken != null || jwtRefreshToken != null) {
                    System.out.println("Valid token found...");
                    // Get user using valid JWT token
                    Optional<? extends EALogin> login = loginService.getUserFromJwt(jwtAccessToken, jwtRefreshToken);
                    if (login != null && login.isPresent()) {
                        setAuthenticationContext(login.get(), jwtAccessToken, jwtRefreshToken);
                    }
                }
            } catch (JwtException e) {
                // Something is wrong with the JWT, don't authenticate
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(EALogin login, String jwtAccessToken, String jwtRefreshToken) {
        List<GrantedAuthority> authorities = calculateAuthorities(login, jwtAccessToken, jwtRefreshToken);
        Authentication auth = new UsernamePasswordAuthenticationToken(login.getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    private List<GrantedAuthority> calculateAuthorities(EALogin login, String jwtAccessToken, String jwtRefreshToken) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (jwtAccessToken != null) {
            authorities.add(new SimpleGrantedAuthority("ACCESS"));  
            for (EAAuthority authority : login.getAuthorities()) {
                authorities.add(authority.generateAuthority());
            }   
        }
        if (jwtRefreshToken != null) {
            authorities.add(new SimpleGrantedAuthority("REFRESH"));
        }
        System.out.println(authorities);
        return authorities;
    }

    /**
     * @param request
     * @param accessToken
     * @return the stored JWT from the header and cookies, otherwise null if not type not present in either.
     * If the JWT is not valid, tampered, or isn't the correct type, then null will be returned. 
     */
    public static String extractValidJwt(HttpServletRequest request, boolean accessToken) {
        String jwtFromCookie = CookieUtil.getJwtFromCookies(request, accessToken);
        String jwtFromHeader = HeaderUtil.getJwtFromHeader(request);
        String[] checkOrder = new String[2];
        int prioritizeOffset = prioritizeHeaders ? 1 : 0;
        checkOrder[prioritizeOffset] = jwtFromCookie;
        checkOrder[1 - prioritizeOffset] = jwtFromHeader;
        for (String jwt : checkOrder) {
            // Verify if the token is the specified value of accessToken
            try {
                boolean isValidType = JwtUtil.isTokenType(jwt, accessToken); 
                if (isValidType) {
                    return jwt;
                }
            } catch (JwtException e) {
                
            } catch (IllegalArgumentException e) {
                
            }
        }
        return null;
    }

}
