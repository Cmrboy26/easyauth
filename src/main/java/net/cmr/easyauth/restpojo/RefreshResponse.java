package net.cmr.easyauth.restpojo;

public class RefreshResponse {

    private String accessToken;

    public RefreshResponse() { }
    public RefreshResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
