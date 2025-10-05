package net.cmr.easyauth.util;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderUtil {
    
    /**
     * @param request
     * @return JWT header from Authorization header (ACCESS token), otherwise null if not present
     */
    public static String getJwtFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
