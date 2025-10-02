package net.cmr.easyauth.entity;

import org.springframework.lang.Nullable;

/**
 * Returned as a result after a login is attempted or a refresh is called. Can be used to update session ids.
 */
public class SessionInfo {
    
    private String jwt;
    private Integer expirationTime;

    public SessionInfo() { }

    public SessionInfo(@Nullable String jwt, @Nullable Integer expirationTime) {
        this.jwt = jwt;
        this.expirationTime = expirationTime;
    }

    public String getJwt() {
        return jwt;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

}
