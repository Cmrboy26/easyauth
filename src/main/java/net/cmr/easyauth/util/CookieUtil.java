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
                if (cookie.getName().equals("jwt-token")) {
                    String accessAttribute = cookie.getAttribute("access");
                    if (accessAttribute != null && accessToken == Boolean.valueOf(accessAttribute)) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }

    public static Cookie generateJwtCookie(String jwt, int jwtDuration, boolean accessToken) {
        Cookie cookie = new Cookie("jwt-token", jwt);
        cookie.setDomain("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtDuration);
        cookie.setAttribute("access", Boolean.toString(accessToken));
        return cookie;
    }

}
