package radioisotops.api.com.example.demo.service;

import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class EmailService {

    // Se cargan desde las variables de entorno de Render
    private final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";

    /**
     * Envío de bienvenida para nuevos médicos
     */
    @Async
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        String html = "<h3>Bienvenido Dr./Dra. " + nombreMedico + "</h3>" +
                "<p>Se ha generado su acceso al Portal Radioisotopo.</p>" +
                "<ul><li><b>Usuario:</b> " + emailDestino + "</li>" +
                "<li><b>Clave:</b> " + passwordTemporal + "</li></ul>";
        ejecutarEnvio(emailDestino, "Bienvenido al Portal Clinico", html);
    }

    /**
     * Envío de nueva clave (Este es el que te pedía el AuthController)
     */
    @Async
    public void enviarPasswordTemporal(String emailDestino, String nombreMedico, String nuevaPassword) {
        String html = "<h3>Restablecimiento de acceso</h3>" +
                "<p>Estimado/a Dr./Dra. " + nombreMedico + ",</p>" +
                "<p>Se ha generado una nueva clave temporal para su cuenta:</p>" +
                "<ul><li><b>Usuario:</b> " + emailDestino + "</li>" +
                "<li><b>Nueva Clave:</b> " + nuevaPassword + "</li></ul>" +
                "<p>Por seguridad, cambiela en su proximo acceso.</p>";
        ejecutarEnvio(emailDestino, "Restablecimiento de acceso - Portal Radioisotopo", html);
    }

    private void ejecutarEnvio(String toEmail, String subject, String htmlContent) {
        try {
            // Verificación de seguridad por si la variable no está en Render
            if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isEmpty()) {
                System.err.println(">>> [SENDGRID-API] ERROR: Variable SENDGRID_API_KEY no encontrada en el sistema.");
                return;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + SENDGRID_API_KEY);

            Map<String, Object> body = new HashMap<>();
            body.put("from", Map.of("email", "radioisotopo.portal@gmail.com", "name", "Portal Radioisotopo"));

            Map<String, Object> personalization = new HashMap<>();
            personalization.put("to", List.of(Map.of("email", toEmail)));
            personalization.put("subject", subject);

            body.put("personalizations", List.of(personalization));
            body.put("content", List.of(Map.of("type", "text/html", "value", htmlContent)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println(">>> [SENDGRID-API] Intentando envio a: " + toEmail);
            restTemplate.postForEntity(SENDGRID_URL, entity, String.class);
            System.out.println(">>> [SENDGRID-API] ¡EXITO! Correo aceptado por SendGrid.");

        } catch (Exception e) {
            System.err.println(">>> [SENDGRID-API] ERROR AL ENVIAR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}