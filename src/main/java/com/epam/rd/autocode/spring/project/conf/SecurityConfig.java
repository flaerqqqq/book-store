package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.security.CustomAuthenticationEntryPoint;
import com.epam.rd.autocode.spring.project.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConf) throws Exception {
        return authConf.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder,
                                                         UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http,
                                                JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.sendError(HttpStatus.NOT_FOUND.value());
                        }).accessDeniedHandler((req, res, authEx) -> {
                            res.sendError(HttpStatus.NOT_FOUND.value());
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConf -> sessionConf.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); ;
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   CustomAuthenticationEntryPoint customAuthEntryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        .requestMatchers("/").permitAll()
                        .requestMatchers("/register", "/login").permitAll()
                        .requestMatchers("/books").permitAll()
                        .requestMatchers("favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**").permitAll()

                        .requestMatchers("/logout").authenticated()
                        .requestMatchers("/shopping-cart", "/shopping-cart/**", "/api/shopping-carts/**").hasRole("CLIENT")
                        .requestMatchers("/orders/checkout").hasRole("CLIENT")
                        .requestMatchers("/orders/**").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/books/new", "/books/*/update", "/books/*/delete").hasAnyRole("EMPLOYEE")

                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthEntryPoint)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken")
                                    .path("/")
                                    .httpOnly(true)
                                    .maxAge(0)
                                    .sameSite(Cookie.SameSite.LAX.toString())
                                    .build();
                            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (authentication != null) {
                                response.sendRedirect("/login?logout");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, LogoutFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConf -> sessionConf.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}