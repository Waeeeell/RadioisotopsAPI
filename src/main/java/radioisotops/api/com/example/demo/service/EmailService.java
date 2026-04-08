package radioisotops.api.com.example.demo.service;

import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class EmailService {

    private final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";
    private final String FRONTEND_URL = "https://radioisotopo.carriedo.cat/login";

    /**
     * Envío de bienvenida para nuevos médicos (CON DISEÑO)
     */
    @Async
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        String htmlContent = generarPlantillaHtml(
                "Bienvenido/a, Dr/a. " + nombreMedico,
                "Se ha generado su acceso al Portal Radioisótopo. Utilice la siguiente contraseña temporal para su primer ingreso:",
                passwordTemporal,
                "Acceder al Portal"
        );
        ejecutarEnvio(emailDestino, "Bienvenido al Portal Clínico", htmlContent);
    }

    /**
     * Envío de nueva clave tras Reset (CON DISEÑO)
     */
    @Async
    public void enviarPasswordTemporal(String emailDestino, String nombreMedico, String passwordTemporal) {
        String htmlContent = generarPlantillaHtml(
                "Restablecimiento de Contraseña",
                "Hola, Dr/a. " + nombreMedico + ". Se ha generado una nueva contraseña temporal para su cuenta:",
                passwordTemporal,
                "Cambiar mi Contraseña"
        );
        ejecutarEnvio(emailDestino, "Nueva contraseña temporal - Portal Médico", htmlContent);
    }

    /**
     * PLANTILLA BASE HTML PARA EVITAR REPETIR CÓDIGO
     */
    private String generarPlantillaHtml(String titulo, String mensaje, String password, String textoBoton) {
        return "<html><body style='font-family: Arial, sans-serif; color: #333; background-color: #f9fafb; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background: white; border: 1px solid #e5e7eb; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #4f46e5; text-align: center;'>" + titulo + "</h2>"
                + "<p style='font-size: 16px; line-height: 1.5;'>" + mensaje + "</p>"
                + "<div style='background: #f3f4f6; padding: 20px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 4px; border-radius: 8px; margin: 25px 0; color: #111827; border: 1px dashed #d1d5db;'>"
                + password + "</div>"
                + "<div style='text-align: center; margin-top: 35px;'>"
                + "<a href='" + FRONTEND_URL + "' style='background-color: #4f46e5; color: white; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block; box-shadow: 0 2px 4px rgba(79, 70, 229, 0.3);'>"
                + textoBoton + "</a>"
                + "</div>"
                + "<hr style='margin-top: 40px; border: 0; border-top: 1px solid #eee;' />"
                + "<p style='font-size: 12px; color: #9ca3af; text-align: center;'>Este es un mensaje automático del sistema de seguridad de Radioisótopos.</p>"
                + "</div></body></html>";
    }

    /**
     * Lógica de conexión con la API de SendGrid
     */
    private void ejecutarEnvio(String toEmail, String subject, String htmlContent) {
        try {
            if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isEmpty()) {
                System.err.println(">>> [SENDGRID-API] ERROR: Variable de entorno no configurada.");
                return;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + SENDGRID_API_KEY);

            Map<String, Object> body = new HashMap<>();
            body.put("from", Map.of("email", "radioisotopo.portal@gmail.com", "name", "Portal Radioisótopo"));

            Map<String, Object> personalization = new HashMap<>();
            personalization.put("to", List.of(Map.of("email", toEmail)));
            personalization.put("subject", subject);

            body.put("personalizations", List.of(personalization));
            body.put("content", List.of(Map.of("type", "text/html", "value", htmlContent)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            System.out.println(">>> [SENDGRID-API] Intentando envío a: " + toEmail);
            restTemplate.postForEntity(SENDGRID_URL, entity, String.class);
            System.out.println(">>> [SENDGRID-API] ¡ÉXITO! Correo enviado correctamente.");

        } catch (Exception e) {
            System.err.println(">>> [SENDGRID-API] ERROR: " + e.getMessage());
        }
    }
}