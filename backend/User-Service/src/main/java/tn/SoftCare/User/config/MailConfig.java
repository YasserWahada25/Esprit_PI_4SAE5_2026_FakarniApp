package tn.SoftCare.User.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Ensures a {@link JavaMailSender} exists when mail auto-configuration does not run.
 * Logs a clear hint if outgoing SMTP is enabled but credentials are missing.
 */
@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    private final Environment environment;

    public MailConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    void warnIfOutgoingMailUnconfigured() {
        if (environment.getProperty("app.mail.mock-send", Boolean.class, false)) {
            return;
        }
        String host = environment.getProperty("spring.mail.host", "");
        String user = environment.getProperty("spring.mail.username", "");
        String pass = environment.getProperty("spring.mail.password", "");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(user) || !StringUtils.hasText(pass)) {
            log.warn(
                    "Mail SMTP is not fully configured in application.properties "
                            + "(spring.mail.host/username/password required).");
        }
    }
}
