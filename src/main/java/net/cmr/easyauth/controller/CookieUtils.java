package net.cmr.easyauth.controller;

import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.cmr.easyauth.service.LoginService;

public class CookieUtils {
    
    public static final String JWT_COOKIE_NAME = "jwt-token";

    public static String getJwtFromCookie(HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static Cookie createJwtCookie(String jwt, int seconds) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, jwt);
        cookie.setHttpOnly(true);
        // TODO: set to true in production
        //cookie.setSecure(true);
        cookie.setPath("/auth/");
        cookie.setMaxAge(seconds);
        if (!cookie.getSecure()) {
            LoginService.logger.warn("IN PRODUCTION, ENSURE cookie.setSecure() IS TRUE!!");
        }
        return cookie;
    }

}
