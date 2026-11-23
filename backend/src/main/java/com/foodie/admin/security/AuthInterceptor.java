package com.foodie.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String PORTAL_IDENTITY_ATTR = "riderIdentity";

    private final PortalAuthService auth;

    public AuthInterceptor(PortalAuthService auth) {
        this.auth = auth;
    }

    public static String tokenOf(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        PortalAuthService.Identity identity = auth.identity(tokenOf(request));
        if (identity != null) {
            request.setAttribute(PORTAL_IDENTITY_ATTR, identity);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"Please log in to continue\"}");
        return false;
    }
}
