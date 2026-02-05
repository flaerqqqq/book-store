package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.trace("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        Cookie cookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);

        if (cookie == null) {
            log.trace("No JWT cookie found for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = cookie.getValue();
            String email = jwtService.extractSubject(accessToken);

            SecurityContext securityContext = SecurityContextHolder.getContext();

            if (email != null && securityContext.getAuthentication() == null) {
                log.debug("Found JWT for user: {}. Attempting authentication.", email);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(accessToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    securityContext.setAuthentication(authentication);

                    log.debug("Successfully authenticated user: {} for URI: {}", email, request.getRequestURI());
                } else {
                    log.warn("Invalid JWT token provided for user: {}", email);
                }
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException authException) {
            log.error("Authentication failed: {}", authException.getMessage());
            customAuthenticationEntryPoint.commence(request, response, authException);
        }
    }
}