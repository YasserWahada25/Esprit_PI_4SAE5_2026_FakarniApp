package tn.SoftCare.User.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import tn.SoftCare.User.dto.*;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.model.Session;
import tn.SoftCare.User.model.User;
import tn.SoftCare.User.repository.SessionRepository;
import tn.SoftCare.User.repository.UserRepository;
import tn.SoftCare.User.security.JwtService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final Environment environment;

    @Value("${app.frontend-url:https://localhost:4200}")
    private String frontendBaseUrl;

    @Value("${app.mail.from:}")
    private String mailFromOverride;

    @Value("${app.mail.logo-path:}")
    private String mailLogoPath;

    /** If true, no e-mail is sent; the reset link is logged at INFO (dev without SMTP). */
    @Value("${app.mail.mock-send:false}")
    private boolean mailMockSend;

    /** Prevent duplicate reset e-mails if user submits multiple times quickly. */
    private final Map<String, Instant> resetEmailLastSentAt = new ConcurrentHashMap<>();

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${facebook.app-id}")
    private String facebookAppId;

    @Value("${facebook.app-secret}")
    private String facebookAppSecret;

    @Value("${facebook.graph-api-version}")
    private String facebookGraphVersion;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AuthService(UserRepository userRepository,
                       SessionRepository sessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JavaMailSender mailSender,
                       Environment environment) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.environment = environment;
    }

    public AuthResponse login(LoginRequest req, String userAgent, String ip) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        // ✅ Comparaison correcte
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        // ✅ Créer une session
        Session s = new Session();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(user.getId());
        s.setCreatedAt(Instant.now());
        s.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshDays() * 24 * 3600));
        s.setRevoked(false);
        s.setUserAgent(userAgent);
        s.setIp(ip);

        // ✅ Générer refresh token (JWT)
        String refreshToken = jwtService.generateRefreshToken(user.getId(), s.getId());

        // ✅ IMPORTANT: on ne fait PAS bcrypt(refreshToken) (limite 72 bytes)
        // On stocke plutôt un SHA-256 du refresh token
        s.setRefreshTokenHash(hashTokenSha256(refreshToken));

        sessionRepository.save(s);

        // ✅ Générer access token
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
            throw new RuntimeException("Refresh token invalide");
        }

        if (!jwtService.isValid(refreshToken)) {
            throw new RuntimeException("Refresh token invalide");
        }

        String userId = jwtService.extractUserId(refreshToken);
        String sessionId = jwtService.extractSessionId(refreshToken);

        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable"));

        if (s.isRevoked() || s.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Session expirée");
        }

        // ✅ Comparer SHA-256(token) avec le hash stocké
        if (!constantTimeEquals(hashTokenSha256(refreshToken), s.getRefreshTokenHash())) {
            throw new RuntimeException("Refresh token invalide");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        // ✅ Rotation du refresh token (même sessionId)
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

    public MessageResponse forgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail() != null ? req.getEmail().trim() : "";
        userRepository.findByEmail(email).ifPresent(user -> {
            Instant now = Instant.now();
            Instant lastSent = resetEmailLastSentAt.get(user.getEmail());
            if (lastSent != null && Duration.between(lastSent, now).getSeconds() < 60) {
                log.info("Skipping duplicate reset email for {} (requested too soon)", user.getEmail());
                return;
            }

            String token = jwtService.generatePasswordResetToken(user.getId());
            String link = frontendBaseUrl + "/auth/reset-password?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);
            if (mailMockSend) {
                log.info("app.mail.mock-send=true - reset link for {}: {}", user.getEmail(), link);
                return;
            }
            try {
                String from = resolveSmtpFromAddress();
                if (!StringUtils.hasText(from)) {
                    log.warn(
                            "spring.mail.username vide dans application.properties. Lien de reinitialisation pour {}: {}",
                            user.getEmail(), link);
                    return;
                }
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                        mime,
                        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                        StandardCharsets.UTF_8.name()
                );
                helper.setFrom(parseAddress(from));
                helper.setTo(parseAddress(user.getEmail().trim()));
                helper.setSubject("Fakarni - Password reset request");
                if (StringUtils.hasText(mailLogoPath)) {
                    FileSystemResource logo = new FileSystemResource(mailLogoPath.trim());
                    if (logo.exists() && logo.isReadable()) {
                        helper.addInline("fakarniLogo", logo);
                    } else {
                        log.warn("Mail logo not found/readable at path: {}", mailLogoPath);
                    }
                }
                helper.setText(buildPasswordResetEmailHtml(user.getPrenom(), link), true);
                mailSender.send(mime);
                resetEmailLastSentAt.put(user.getEmail(), now);
                log.info("E-mail de reinitialisation envoye vers {}", user.getEmail());
            } catch (Exception e) {
                log.warn(
                        "E-mail reset impossible pour {} ({}). Lien de secours (copier pour test local): {}",
                        user.getEmail(), e.getMessage(), link);
            }
        });
        return new MessageResponse(
                "Si un compte existe pour cet e-mail, un lien de réinitialisation a été envoyé.");
    }

    public MessageResponse resetPassword(ResetPasswordRequest req) {
        String userId = jwtService.parsePasswordResetTokenSubject(req.getToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Votre mot de passe a été mis à jour.");
    }

    public AuthResponse googleLogin(GoogleLoginRequest req, String userAgent, String ip) {
        // 1. Vérifier le credential Google
        GoogleIdToken.Payload payload = verifyGoogleToken(req.getCredential());

        String email = payload.getEmail();
        String given = (String) payload.get("given_name");
        String family = (String) payload.get("family_name");
        final String firstName = given != null ? given : "";
        final String lastName = family != null ? family : "";

        // 2. Chercher ou créer l'utilisateur
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setEmail(email);
            newUser.setPrenom(firstName);
            newUser.setNom(lastName);
            newUser.setPassword(""); // pas de mot de passe pour les comptes Google
            newUser.setRole(Role.PATIENT_PROFILE);
            return userRepository.save(newUser);
        });

        // 3. Créer une session et retourner les tokens
        Session s = new Session();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(user.getId());
        s.setCreatedAt(Instant.now());
        s.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshDays() * 24 * 3600));
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

    public AuthResponse facebookLogin(FacebookLoginRequest req, String userAgent, String ip) {
        String accessToken = req.getAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("Token Facebook manquant");
        }

        verifyFacebookAccessToken(accessToken);
        FacebookProfile fb = fetchFacebookProfile(accessToken);

        if (fb.id() == null || fb.id().isBlank()) {
            throw new RuntimeException("Profil Facebook sans identifiant.");
        }

        // If the app does not use the email permission (Meta "Invalid Scopes: email"), Graph omits email — use a stable synthetic address.
        String email = fb.email();
        if (email == null || email.isBlank()) {
            email = "fb_" + fb.id() + "@facebook.fakarni";
        }

        final String resolvedEmail = email;
        String firstName = fb.firstName() != null ? fb.firstName() : "";
        String lastName = fb.lastName() != null ? fb.lastName() : "";

        User user = userRepository.findByEmail(resolvedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(UUID.randomUUID().toString());
            newUser.setEmail(resolvedEmail);
            newUser.setPrenom(firstName);
            newUser.setNom(lastName);
            newUser.setPassword("");
            newUser.setRole(Role.PATIENT_PROFILE);
            return userRepository.save(newUser);
        });

        Session s = new Session();
        s.setId(UUID.randomUUID().toString());
        s.setUserId(user.getId());
        s.setCreatedAt(Instant.now());
        s.setExpiresAt(Instant.now().plusSeconds(jwtService.getRefreshDays() * 24 * 3600));
        s.setRevoked(false);
        s.setUserAgent(userAgent);
        s.setIp(ip);

        String refreshToken = jwtService.generateRefreshToken(user.getId(), s.getId());
        s.setRefreshTokenHash(hashTokenSha256(refreshToken));
        sessionRepository.save(s);

        String jwtAccess = jwtService.generateAccessToken(user);

        AuthResponse res = new AuthResponse();
        res.setAccessToken(jwtAccess);
        res.setRefreshToken(refreshToken);
        res.setUser(toUserResponse(user));
        return res;
    }

    private void verifyFacebookAccessToken(String userAccessToken) {
        try {
            String appToken = facebookAppId + "|" + facebookAppSecret;
            String url = "https://graph.facebook.com/" + facebookGraphVersion + "/debug_token?input_token="
                    + URLEncoder.encode(userAccessToken, StandardCharsets.UTF_8)
                    + "&access_token="
                    + URLEncoder.encode(appToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Échec de la vérification du token Facebook (HTTP " + response.statusCode() + ")");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");
            if (!data.path("is_valid").asBoolean(false)) {
                throw new RuntimeException("Token Facebook invalide ou expiré");
            }
            String tokenAppId = data.path("app_id").asText("");
            if (!facebookAppId.equals(tokenAppId)) {
                throw new RuntimeException("Token Facebook ne correspond pas à cette application");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de vérification Facebook: " + e.getMessage(), e);
        }
    }

    private FacebookProfile fetchFacebookProfile(String userAccessToken) {
        try {
            String url = "https://graph.facebook.com/" + facebookGraphVersion + "/me?fields=id,email,first_name,last_name&access_token="
                    + URLEncoder.encode(userAccessToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                JsonNode err = objectMapper.readTree(response.body()).path("error");
                String msg = err.path("message").asText("Erreur API Facebook");
                throw new RuntimeException(msg);
            }

            JsonNode root = objectMapper.readTree(response.body());
            return new FacebookProfile(
                    root.path("id").asText(null),
                    root.path("email").asText(null),
                    root.path("first_name").asText(null),
                    root.path("last_name").asText(null)
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture profil Facebook: " + e.getMessage(), e);
        }
    }

    private record FacebookProfile(String id, String email, String firstName, String lastName) {}

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Token Google invalide ou expiré");
            }
            return idToken.getPayload();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de vérification du token Google: " + e.getMessage(), e);
        }
    }

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

    private String resolveSmtpFromAddress() {
        if (StringUtils.hasText(mailFromOverride)) {
            return mailFromOverride.trim();
        }
        String fromProps = environment.getProperty("spring.mail.username");
        if (StringUtils.hasText(fromProps)) {
            return fromProps.trim();
        }
        if (mailSender instanceof JavaMailSenderImpl impl) {
            String u = impl.getUsername();
            if (StringUtils.hasText(u)) {
                return u.trim();
            }
        }
        return null;
    }

    private static InternetAddress parseAddress(String raw) {
        try {
            return new InternetAddress(raw.trim(), false);
        } catch (Exception e) {
            throw new IllegalArgumentException("Adresse e-mail invalide: " + raw, e);
        }
    }

    private String buildPasswordResetEmailHtml(String firstName, String link) {
        String safeFirstName = StringUtils.hasText(firstName) ? firstName.trim() : "there";
        return """
                <div style="font-family:Arial,Helvetica,sans-serif;line-height:1.6;color:#1f2937;max-width:620px;margin:0 auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px;">
                  <div style="display:flex;align-items:center;justify-content:center;background:#f5efff;border-radius:10px;padding:12px;margin:0 0 18px 0;">
                    <img src="cid:fakarniLogo" alt="Fakarni" style="max-width:360px;width:100%%;height:auto;display:block;" />
                  </div>
                  <h2 style="margin:0 0 8px 0;color:#111827;">Reset your password</h2>
                  <p style="margin:0 0 16px 0;">Hi %s,</p>
                  <p style="margin:0 0 16px 0;">We received a request to reset your Fakarni account password.</p>
                  <p style="margin:0 0 24px 0;">
                    <a href="%s" style="display:inline-block;background:#6f4ca3;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:8px;font-weight:600;">
                      Reset Password
                    </a>
                  </p>
                  <p style="margin:0 0 8px 0;">This link is valid for <strong>1 hour</strong>.</p>
                  <p style="margin:0 0 8px 0;">If you did not request this, you can safely ignore this email.</p>
                </div>
                """.formatted(safeFirstName, link);
    }

    /**
     * Hash SHA-256 du token, encodé en Base64 (stockage DB friendly)
     */
    private String hashTokenSha256(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Erreur hashing refresh token", e);
        }
    }

    /**
     * Comparaison en temps constant (évite timing attacks)
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}