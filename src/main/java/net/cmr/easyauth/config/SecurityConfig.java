package net.cmr.easyauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import net.cmr.easyauth.security.DatabaseUserDetailsService;
import net.cmr.easyauth.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/h2-console/**").hasRole("ADMIN");
            })
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/auth/register").permitAll();
                auth.requestMatchers("/auth/login").permitAll();
                auth.requestMatchers("/auth/verify").permitAll();
                auth.requestMatchers("/auth/refresh").authenticated();
                auth.requestMatchers("/auth/all").authenticated();
            })
            .authorizeHttpRequests(auth -> {
                auth.anyRequest().permitAll();
            })
            .csrf(csrf -> csrf.disable()) // needed for H2 console
            .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable()); // needed for H2 console frames
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); 
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new DatabaseUserDetailsService();
    }

    public static boolean isValidRole(String role) {
        return role.equals("USER") || role.equals("ADMIN");
    }

}
