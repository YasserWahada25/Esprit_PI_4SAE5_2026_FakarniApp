package tn.SoftCare.User.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tn.SoftCare.User.security.SessionAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }

    /**
     * CORS is handled only by {@code Gateway-Service} (port 8090). Adding CORS here too
     * duplicates {@code Access-Control-Allow-Origin} and browsers reject the response.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(401)
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/google",
                                "/auth/facebook",
                                "/auth/logout",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/internal/users/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}