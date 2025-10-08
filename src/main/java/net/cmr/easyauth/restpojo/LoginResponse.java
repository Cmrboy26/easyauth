package net.cmr.easyauth.restpojo;

public class LoginResponse {
    
    private String refreshToken;
    private String accessToken;

    public LoginResponse() { }
    public LoginResponse(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
