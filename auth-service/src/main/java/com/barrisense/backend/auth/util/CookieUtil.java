package com.barrisense.backend.auth.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    public static Cookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds, String path, boolean secure, String sameSite) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setPath(path);
        // SameSite is not part of Cookie API; set via response header in controller
        return cookie;
    }

    public static String formatCookieHeader(String name, String value, int maxAgeSeconds) {
        return String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                name, value, maxAgeSeconds);
    }

}
