package com.foodie.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/** Restricts the admin API to a valid admin bearer-token session. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ATTR = "adminUsername";

    private final AuthService auth;

    public AuthInterceptor(AuthService auth) {
        this.auth = auth;
    }

    public static String tokenOf(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (h != null && h.startsWith("Bearer ")) ? h.substring(7).trim() : null;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;   // CORS pre-flight

        String path = req.getRequestURI();
        if (path.startsWith("/api/public/images/")) return true;
        if (path.equals("/api/auth/password-reset/request")
                || path.equals("/api/auth/password-reset/confirm")) return true;

        String username = auth.usernameFor(tokenOf(req));
        if (username != null) {
            req.setAttribute(USER_ATTR, username);
            return true;
        }
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"message\":\"Please log in to continue\"}");
        return false;
    }
}
