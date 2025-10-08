package net.cmr.easyauth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {
    
    /**
     * @param request
     * @return null if no cookie was found, JWT as a string if found
     */
    public static String getJwtFromCookies(HttpServletRequest request, boolean accessToken) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken") || cookie.getName().equals("refreshToken")) {
                    boolean isAccessToken = cookie.getName().equals("accessToken");
                    if (accessToken == isAccessToken) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static Cookie generateJwtCookie(String jwt, boolean accessToken) {
        return generateJwtCookie(jwt, accessToken ? JwtUtil.accessExpirationTime : JwtUtil.refreshExpirationTime, accessToken);
    }
    
    public static Cookie generateJwtCookie(String jwt, int jwtDuration, boolean accessToken) {
        Cookie cookie = new Cookie(accessToken ? "accessToken" : "refreshToken", jwt);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtDuration);
        return cookie;
    }

}
