package net.cmr.easyauth.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class EALoginDetails implements UserDetails {

    private EALogin login;
    private Collection<GrantedAuthority> grantedAuthorities;

    public EALoginDetails(EALogin login, Collection<GrantedAuthority> authorities) {
        this.login = login;
        this.grantedAuthorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return login.getPassword();
    }

    @Override
    public String getUsername() {
        return login.getUsername();
    }
    
}
