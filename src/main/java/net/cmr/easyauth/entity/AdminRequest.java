package net.cmr.easyauth.entity;

/**
 * Used by admins to elevate the role of any user.
 */
public class AdminRequest {
    
    private String value;
    private String primaryCredential;
    
    public AdminRequest(String primaryCredential, String value) {
        this.primaryCredential = primaryCredential;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getPrimaryCredential() {
        return primaryCredential;
    }

}
