package net.cmr.easyauth.controller;

import net.cmr.easyauth.entity.Login;

/**
 * Can be ensured to be non-null in any @RequestMapping method if @PreAuthorize("hasRole('USER')") is present
 */
public class AuthContext {
    
    private Login login;
    private String jwt;
    
    protected Login getLogin() { return login; }
    protected void setLogin(Login login) { this.login = login; }

    public String getJwt() { return jwt; }
    public void setJwt(String jwt) { this.jwt = jwt; }

}
