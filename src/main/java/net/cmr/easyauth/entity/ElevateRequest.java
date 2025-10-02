package net.cmr.easyauth.entity;

/**
 * Used by admins to elevate the role of any user.
 */
public class ElevateRequest {
    
    private String role;
    private String primaryCredential;
    
    public ElevateRequest(String primaryCredential, String role) {
        this.primaryCredential = primaryCredential;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getPrimaryCredential() {
        return primaryCredential;
    }

}
