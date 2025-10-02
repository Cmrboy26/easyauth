package net.cmr.easyauth.entity;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class RegisterRequest {
    
    private String email;
    private String username;
    private String password;

    public RegisterRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPlaintextPassword() {
        return password;
    }

}
