package net.cmr.easyauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import net.cmr.easyauth.entity.EALogin;
import net.cmr.easyauth.filter.JwtAuthenticationFilter;
import net.cmr.easyauth.service.EALoginService;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired private EALoginService<? extends EALogin> loginService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(loginService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        http.authorizeHttpRequests(auth -> {
                auth.requestMatchers("/h2-console/**").hasRole("ADMIN");
            })
            /*.authorizeHttpRequests(auth -> {
                auth.requestMatchers("/auth/register").permitAll();
                auth.requestMatchers("/auth/login").permitAll();
                auth.requestMatchers("/auth/verify").permitAll();
                auth.requestMatchers("/auth/refresh").authenticated();
                auth.requestMatchers("/auth/authorities").authenticated();
            })*/
            .authorizeHttpRequests(auth -> {
                auth.anyRequest().permitAll();
            })
            .csrf(csrf -> csrf.disable()) // needed for H2 console
            .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable()); // needed for H2 console frames
        http.addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
