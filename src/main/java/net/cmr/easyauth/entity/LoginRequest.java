package net.cmr.easyauth.entity;

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
