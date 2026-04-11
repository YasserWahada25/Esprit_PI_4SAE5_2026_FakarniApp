package tn.SoftCare.User.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.SoftCare.User.dto.*;
import tn.SoftCare.User.model.PasswordResetToken;
import tn.SoftCare.User.model.Session;
import tn.SoftCare.User.model.User;
import tn.SoftCare.User.repository.PasswordResetTokenRepository;
import tn.SoftCare.User.repository.SessionRepository;
import tn.SoftCare.User.repository.UserRepository;
import tn.SoftCare.User.security.JwtService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ✅ Injected for forgot/reset
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    // ✅ Config
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
    private static final String FRONT_RESET_URL = "http://localhost:4200/auth/reset-password?token=";

    // ✅ Same password rule as backend
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{6,}$");

    public AuthService(UserRepository userRepository,
                       SessionRepository sessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService; // ✅ yes, injected here
    }

    public AuthResponse login(LoginRequest req, String userAgent, String ip) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email or password is incorrect."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email or password is incorrect.");
        }

        Session s = new Session();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(user.getId());
        s.setCreatedAt(Instant.now());
        s.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshDays() * 24 * 3600L));
        s.setRevoked(false);
        s.setUserAgent(userAgent);
        s.setIp(ip);

        String refreshToken = jwtService.generateRefreshToken(user.getId(), s.getId());
        s.setRefreshTokenHash(hashTokenSha256(refreshToken));
        sessionRepository.save(s);

        String accessToken = jwtService.generateAccessToken(user);

        AuthResponse res = new AuthResponse();
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setUser(toUserResponse(user));
        return res;
    }

    public AuthResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Invalid refresh token.");
        }

        if (!jwtService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token.");
        }

        String userId = jwtService.extractUserId(refreshToken);
        String sessionId = jwtService.extractSessionId(refreshToken);

        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found."));

        if (s.isRevoked() || s.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Session expired.");
        }

        if (!constantTimeEquals(hashTokenSha256(refreshToken), s.getRefreshTokenHash())) {
            throw new RuntimeException("Invalid refresh token.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));

        String newRefresh = jwtService.generateRefreshToken(userId, sessionId);
        s.setRefreshTokenHash(hashTokenSha256(newRefresh));
        sessionRepository.save(s);

        AuthResponse res = new AuthResponse();
        res.setAccessToken(jwtService.generateAccessToken(user));
        res.setRefreshToken(newRefresh);
        res.setUser(toUserResponse(user));
        return res;
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        if (!jwtService.isValid(refreshToken)) return;

        String sessionId = jwtService.extractSessionId(refreshToken);

        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setRevoked(true);
            sessionRepository.save(s);
        });
    }

    // ============================================================
    // ✅ FORGOT / RESET PASSWORD
    // ============================================================

    /**
     * Always returns OK (security). If email exists -> send reset link.
     */
    public void forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            // ✅ do nothing (still "success" at controller level)
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // ✅ do not reveal account existence
            return;
        }

        // ✅ delete old tokens for that user (clean)
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashTokenSha256(rawToken);

        PasswordResetToken t = new PasswordResetToken();
        t.setId(UUID.randomUUID().toString());
        t.setUserId(user.getId());
        t.setEmail(user.getEmail());
        t.setTokenHash(tokenHash);
        t.setCreatedAt(Instant.now());
        t.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL));
        t.setUsed(false);

        passwordResetTokenRepository.save(t);

        String link = FRONT_RESET_URL + rawToken;

        // ✅ YES: send to the user's email
        emailService.send(
                user.getEmail(),
                "Reset your password",
                "Click the link below to reset your password (valid for 15 minutes):\n" + link
        );
    }

    /**
     * Validate token then update password.
     */
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new RuntimeException("Token is required.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("New password is required.");
        }

        // ✅ enforce same password strength as create/update
        if (!STRONG_PASSWORD.matcher(newPassword).matches()) {
            throw new RuntimeException("Password must be at least 6 characters and contain 1 uppercase, 1 number, and 1 special character.");
        }

        String tokenHash = hashTokenSha256(rawToken);

        PasswordResetToken t = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token."));

        if (t.isUsed()) {
            throw new RuntimeException("Token has already been used.");
        }

        if (t.getExpiresAt() == null || t.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Token has expired.");
        }

        User user = userRepository.findById(t.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        t.setUsed(true);
        passwordResetTokenRepository.save(t);

        // ✅ optional security: revoke all sessions
        List<Session> sessions = sessionRepository.findAllByUserId(user.getId());
        sessions.forEach(s -> s.setRevoked(true));
        sessionRepository.saveAll(sessions);
    }

    // ============================================================

    private UserResponse toUserResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole());
        r.setNumTel(u.getNumTel());
        r.setAdresse(u.getAdresse());
        return r;
    }

    private String hashTokenSha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing error", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}