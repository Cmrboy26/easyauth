package net.cmr.easyauth.resolver;

import net.cmr.easyauth.entity.EALogin;

/**
 * Can be ensured to be non-null in any @RequestMapping method if @PreAuthorize("hasRole('USER')") is present
 */
public class AuthContext<T extends EALogin> {
    
    private T login;
    private String accessJwt;
    private String refreshJwt;
    
    protected T getLogin() { return login; }
    protected void setLogin(T login) { this.login = login; }

    public String getRefreshJwt() { return refreshJwt; }
    public void setRefreshJwt(String refreshJwt) { this.refreshJwt = refreshJwt; }

    public String getAccessJwt() { return accessJwt; }
    public void setAccessJwt(String accessJwt) { this.accessJwt = accessJwt; }

}
