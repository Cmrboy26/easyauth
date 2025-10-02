package net.cmr.easyauth.entity;

import java.security.InvalidParameterException;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "LOGIN", uniqueConstraints = @UniqueConstraint(columnNames = {"ID", "EMAIL"}))
public class Login {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;
    @Column(name = "EMAIL", nullable = false)
    private String email;
    @Column(name = "USERNAME", nullable = false)
    private String username;
    @Column(name = "ROLE", nullable = false)
    private String role;
    @Column(name = "PASSWORD", nullable = false)
    private String passwordHash;

    public static final String USER_ROLE = "USER";

    public Login() { /* No-args constructor */}

    public Login(RegisterRequest registerRequest) {
        this(registerRequest.getEmail(), registerRequest.getUsername(), registerRequest.getPlaintextPassword(), USER_ROLE);
    }

    public Login(String email, String username, String password, String role) {
        this.id = null;
        this.email = email;
        this.username = username;
        this.role = role;
        String salt = BCrypt.gensalt();
        this.passwordHash = BCrypt.hashpw(password, salt);
    }


    public static String getEmailFromCombinedCredential(String combinedCredential) throws InvalidParameterException {
        String[] split = combinedCredential.split(":");
        if (split.length != 2) {
            throw new InvalidParameterException();
        }
        return split[0];
    }
    
    public static String getUsernameFromCombinedCredential(String combinedCredential) throws InvalidParameterException {
        String[] split = combinedCredential.split(":");
        if (split.length != 2) {
            throw new InvalidParameterException();
        }
        return split[1];
    }
    
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getCombinedPrimaryCredential() {
        return getEmail() + ":" + getUsername();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
