package com.barrisense.backend.auth.util;

public class CookieUtil {

    public static String formatCookieHeader(String name, String value, int maxAgeSeconds) {
        return String.format("%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
                name, value, maxAgeSeconds);
    }

}
