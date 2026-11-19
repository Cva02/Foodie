package com.foodie.customer.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenService tokens;

    public AuthInterceptor(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if ("OPTIONS".equals(req.getMethod())) return true;
        String path = req.getRequestURI();
        boolean isPublic = path.startsWith("/api/auth/")
                || ("GET".equals(req.getMethod()) && path.startsWith("/api/restaurants"));
        String header = req.getHeader("Authorization");
        Long uid = (header != null && header.startsWith("Bearer ")) ? tokens.verify(header.substring(7)) : null;
        if (uid != null) req.setAttribute("uid", uid);
        if (isPublic || uid != null) return true;
        res.setStatus(401);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"Please log in to continue\"}");
        return false;
    }
}
