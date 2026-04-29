package tn.SoftCare.User.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tn.SoftCare.User.service.AuthService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionCookieService sessionCookieService;
    private final AuthService authService;

    public SessionAuthenticationFilter(SessionCookieService sessionCookieService, AuthService authService) {
        this.sessionCookieService = sessionCookieService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> sessionId = sessionCookieService.extractSessionId(request);

        if (sessionId.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            authService.findAuthenticatedUserBySessionId(sessionId.get()).ifPresent(user -> {
                AuthenticatedUser principal = new AuthenticatedUser(
                        sessionId.get(),
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
