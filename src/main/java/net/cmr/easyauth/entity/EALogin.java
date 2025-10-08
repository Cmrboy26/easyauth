package net.cmr.easyauth.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import net.cmr.easyauth.util.NonNullMap;
import net.cmr.easyauth.util.NonNullMap.NullValueException;

@MappedSuperclass
public abstract class EALogin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * User password hashed by BCrypt
     */
    @Column(name = "password", nullable = false, unique = true)
    private String password;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "login_authorities",
        joinColumns = @JoinColumn(name = "login_id"),
        inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private List<EAAuthority> authorities = new ArrayList<>();

    public EALogin() { }

    /**
     * Constructs a login using the provided registerParams map, which contains
     * client-supplied key-value pairs. Input should be validated for safety.
     * @param registerParams map of registration parameters
     * @throws NullValueException if required values are missing
     */
    public EALogin(Map<String, String> registerParams) throws NullValueException {
        this.username = registerParams.get("username");
        this.password = hashPassword(registerParams.get("password"));
    }

    protected String hashPassword(String password) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(password, salt);
    }

    public boolean checkPassword(String testPassword) {
        return BCrypt.checkpw(testPassword, password);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public List<EAAuthority> getAuthorities() { return authorities; }
    public void setAuthorities(List<EAAuthority> authorities) { this.authorities = authorities; }
    
    // Utility methods for authority management
    public void addAuthority(EAAuthority authority) {
        if (!authorities.contains(authority)) {
            authorities.add(authority);
        }
    }
    
    public void removeAuthority(EAAuthority authority) {
        authorities.remove(authority);
    }
    
    public boolean hasAuthority(String authorityValue) {
        return authorities.stream()
            .anyMatch(auth -> authorityValue.equals(auth.getAuthorityValue()));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EALogin && ((EALogin) obj).getId() == getId();
    }
}
