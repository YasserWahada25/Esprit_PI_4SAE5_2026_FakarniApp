package tn.SoftCare.Geofencing.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.SoftCare.Geofencing.Entity.Alert;
import tn.SoftCare.Geofencing.Entity.NotificationPreference;
import tn.SoftCare.Geofencing.dto.UserDto;

import java.util.Properties;

@Service
public class NotificationService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String emailPassword;

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token}")
    private String twilioAuthToken;

    @Value("${twilio.from-number}")
    private String twilioFromNumber;

    // ─────────────────────────────────────────────────────────────
    //  ALERTES DE ZONE — Email uniquement selon préférences
    // ─────────────────────────────────────────────────────────────
    public void notifySoignant(UserDto soignant, Alert alert,
                               NotificationPreference pref) {
        if (pref.isEmailEnabled()) {
            if (soignant.getEmail() != null && !soignant.getEmail().isEmpty()) {
                sendAlertEmail(soignant.getEmail(), alert);
            } else {
                System.err.println("⚠️ Email soignant absent");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SOS — Appel vocal immédiat
    // ─────────────────────────────────────────────────────────────
    public void sendSosVoiceCall(String toPhone, String patientName) {
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);

            String voiceMessage = String.format(
                    "Urgence Fakarni ! " +
                            "Le patient %s a déclenché une alerte S.O.S. " +
                            "Contactez immédiatement ce patient. " +
                            "Je répète. Le patient %s a déclenché une alerte S.O.S.",
                    patientName, patientName
            );

            String twimlXml = String.format("""
                <Response>
                    <Say language="fr-FR" voice="woman">%s</Say>
                </Response>
                """, voiceMessage);

            Call.creator(
                    new PhoneNumber(formatPhone(toPhone)),
                    new PhoneNumber(twilioFromNumber),
                    new Twiml(twimlXml)
            ).create();

            System.out.println("✅ Appel SOS lancé vers : " + formatPhone(toPhone));

        } catch (Exception e) {
            System.err.println("❌ Erreur appel SOS : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SOS — Email avec position GPS
    // ─────────────────────────────────────────────────────────────
    public void sendSosEmail(String toEmail, String patientName,
                             String patientId, double lat, double lon) {
        try {
            JavaMailSenderImpl mailSender = buildMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🆘 SOS — " + patientName + " a besoin d'aide !");

            String googleMapsLink = String.format(
                    "https://www.google.com/maps?q=%s,%s", lat, lon
            );

            String html = """
                <div style="font-family:Arial,sans-serif;padding:24px;
                            background:#fff3f3;border-left:5px solid #e53935;">
                    <h2 style="color:#e53935;margin-top:0;">🆘 ALERTE SOS</h2>
                    <p style="font-size:16px;">
                        Le patient <b>%s</b> a appuyé sur le bouton SOS.
                    </p>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr style="background:#ffeaea;">
                            <td style="padding:8px;font-weight:bold;width:40%%">Patient</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px;font-weight:bold;">ID Patient</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr style="background:#ffeaea;">
                            <td style="padding:8px;font-weight:bold;">Latitude</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px;font-weight:bold;">Longitude</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                    </table>
                    <div style="margin-top:20px;text-align:center;">
                        <a href="%s"
                           style="background:#e53935;color:white;padding:12px 24px;
                                  border-radius:8px;text-decoration:none;
                                  font-weight:bold;font-size:14px;">
                            📍 Voir position sur Google Maps
                        </a>
                    </div>
                    <p style="margin-top:20px;color:#555;font-size:13px;">
                        ⚠️ Contactez immédiatement ce patient.
                    </p>
                </div>
                """.formatted(
                    patientName, patientName, patientId,
                    lat, lon, googleMapsLink
            );

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email SOS envoyé à : " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Erreur email SOS : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  EMAIL ALERTE DE ZONE
    // ─────────────────────────────────────────────────────────────
    private void sendAlertEmail(String toEmail, Alert alert) {
        try {
            JavaMailSenderImpl mailSender = buildMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🚨 ALERTE — Patient " + alert.getPatientName() + " hors zone");

            String html = """
                <div style="font-family:Arial,sans-serif;padding:24px;
                            background:#fff3f3;border-left:5px solid #e74c3c;">
                    <h2 style="color:#e74c3c;margin-top:0;">🚨 Alerte de Géolocalisation</h2>
                    <table style="width:100%%;border-collapse:collapse;font-size:14px;">
                        <tr style="background:#ffeaea;">
                            <td style="padding:8px;font-weight:bold;width:40%%">Patient</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px;font-weight:bold;">Type d'alerte</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                        <tr style="background:#ffeaea;">
                            <td style="padding:8px;font-weight:bold;">Sévérité</td>
                            <td style="padding:8px;color:%s;font-weight:bold;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding:8px;font-weight:bold;">Distance hors zone</td>
                            <td style="padding:8px;">%.0f mètres</td>
                        </tr>
                        <tr style="background:#ffeaea;">
                            <td style="padding:8px;font-weight:bold;">Date / Heure</td>
                            <td style="padding:8px;">%s</td>
                        </tr>
                    </table>
                    <p style="margin-top:20px;color:#555;font-size:13px;">
                        ⚠️ Veuillez vérifier immédiatement la localisation du patient.
                    </p>
                </div>
                """.formatted(
                    alert.getPatientName(),
                    alert.getType(),
                    severityColor(alert.getSeverity()), alert.getSeverity(),
                    alert.getDistanceHorsZone(),
                    alert.getTimestamp()
            );

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email alerte envoyé à : " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Erreur email : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private JavaMailSenderImpl buildMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(fromEmail);
        mailSender.setPassword(emailPassword);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        return mailSender;
    }

    private String formatPhone(String phone) {
        String cleaned = phone.replaceAll("[\\s\\-().]+", "");
        if (cleaned.startsWith("+"))  return cleaned;
        if (cleaned.startsWith("00")) return "+" + cleaned.substring(2);
        if (cleaned.startsWith("0"))  return "+216" + cleaned.substring(1);
        return "+216" + cleaned;
    }

    private String severityColor(String severity) {
        return switch (severity) {
            case "High"   -> "#e53935";
            case "Medium" -> "#fb8c00";
            default       -> "#43a047";
        };
    }
}