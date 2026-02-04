package com.epam.rd.autocode.spring.project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    public static boolean hasAuthority(String role) {
        Authentication auth = checkAndReturn();

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    public static boolean isClient() {
        return hasAuthority("ROLE_CLIENT");
    }

    public static UUID getCurrentUserPublicId() {
        CustomUserDetails userDetails = getCurrentUser();

        if (userDetails != null) {
            return userDetails.getPublicId();
        }
        return null;
    }

    private static CustomUserDetails getCurrentUser() {
        Authentication auth = checkAndReturn();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }

    private static Authentication checkAndReturn(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return auth;
    }
}