package net.cmr.easyauth.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "LOGIN", uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "EMAIL"}))
public class Login implements UserDetails {
    
    /*
     * TODO: Library users should be able to store additional information in users.
     * Find a way for programmers to create supplementary information for logins
     * using foreign keys (one to many OR one to one support)
     */
    public static final String USER_ROLE = "USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;
    // TODO: get rid of this, have username be the primary credential
    @Deprecated
    @Column(name = "EMAIL", nullable = false)
    private String email;
    @Column(name = "USERNAME", nullable = false)
    private String username;
    @Column(name = "ROLE", nullable = false)
    private String role;
    @JsonIgnore
    @Column(name = "PASSWORD", nullable = false)
    private String passwordHash;
    @Column(name = "LOCKED", nullable = false)
    private boolean locked;

    private Collection<GrantedAuthority> authorities;

    public Login(RegisterRequest registerRequest, Collection<GrantedAuthority> authorities) {
        this(registerRequest.getEmail(), registerRequest.getUsername(), registerRequest.getPlaintextPassword(), authorities);
    }

    public Login(String email, String username, String password, Collection<GrantedAuthority> authorities) {
        this.id = null;
        this.email = email;
        this.username = username;
        this.authorities = Collections.unmodifiableCollection(authorities);

        // this.role = role;
        String salt = BCrypt.gensalt();
        this.passwordHash = BCrypt.hashpw(password, salt);
        this.locked = false;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Deprecated
    public String getEmail() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    public void setLocked(boolean shouldLock) {
        this.locked = shouldLock;
    }

}
