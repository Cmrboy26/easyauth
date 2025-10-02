package net.cmr.easyauth.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import net.cmr.easyauth.entity.Login;
import net.cmr.easyauth.repository.LoginRepository;

@Component
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired LoginRepository loginRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Login login = loginRepository.findByUsernameOrEmail(username, username);
        if (login == null) {
            throw new UsernameNotFoundException(username + " not found in email or username columns in database");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_"+login.getRole()));
        User user = new User(login.getUsername(), null, authorities);
        return user;
    }
}
