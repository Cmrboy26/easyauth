package net.cmr.easyauth.entity;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.springframework.boot.util.LambdaSafe.Callback;
import org.springframework.security.crypto.bcrypt.BCrypt;

import net.cmr.easyauth.repository.LoginRepository;

public class LoginRequest {
    
    /**
     * Can be either a username or email.
     */
    private String primaryCredential;
    private String password;

    public LoginRequest(String primaryCredential, String password) {
        this.primaryCredential = primaryCredential;
        this.password = password;
    }

    public String getPrimaryCredential() {
        return primaryCredential;
    }

    public String getPlaintextPassword() {
        return password;
    }

}
