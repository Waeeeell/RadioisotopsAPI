package radioisotops.api.com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envío de nueva clave temporal tras un reset en el Panel de Auditoría.
     * Al ser @Async, los errores se manejan internamente mediante logs.
     */
    @Async
    public void enviarPasswordTemporal(String emailDestino, String nombreMedico, String nuevaPassword) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Restablecimiento de acceso - radioisotopo.portal");

            String contenido = "Estimado/a Dr./Dra. " + nombreMedico + ",\n\n" +
                    "Le informamos de que se ha generado una nueva clave temporal para su acceso a la plataforma de monitorizacion nuclear.\n\n" +
                    "• Usuario: " + emailDestino + "\n" +
                    "• Clave temporal: " + nuevaPassword + "\n\n" +
                    "Por motivos de seguridad y para garantizar la proteccion de los datos de nuestro hospital, le recomendamos cambiar su contrasena en su proximo acceso.\n\n" +
                    "Si necesita asistencia adicional, estaremos encantados de ayudarle.\n\n" +
                    "Un cordial saludo,\n" +
                    "Departamento de Seguridad IT";

            mensaje.setText(contenido);

            System.out.println(">>> [ASYNC-MAIL] Iniciando reset de password para: " + emailDestino);
            mailSender.send(mensaje);
            System.out.println(">>> [ASYNC-MAIL] Email de reset enviado con exito.");

        } catch (MailException e) {
            System.err.println(">>> [ASYNC-MAIL] ERROR en reset de password:");
            System.err.println(">>> Detalle: " + e.getMessage());
            // No lanzamos RuntimeException para evitar excepciones huerfanas en hilos async
        }
    }

    /**
     * Envío de bienvenida y credenciales para nuevos médicos registrados.
     */
    @Async
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        try {
            System.out.println(">>> [ASYNC-MAIL] Preparando bienvenida para: " + emailDestino);

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Bienvenido al Portal Clinico - radioisotopo.portal");

            String contenido = "Estimado/a Dr./Dra. " + nombreMedico + ",\n\n" +
                    "Su cuenta ha sido creada. Credenciales:\n" +
                    "• Usuario: " + emailDestino + "\n" +
                    "• Clave: " + passwordTemporal + "\n\n" +
                    "Acceda aqui: https://radioisotopo.carriedo.cat";

            mensaje.setText(contenido);

            System.out.println(">>> [ASYNC-MAIL] Conectando con servidor SMTP...");
            mailSender.send(mensaje);
            System.out.println(">>> [ASYNC-MAIL] EXITOSO: El correo de bienvenida ha sido enviado.");

        } catch (Exception e) {
            System.err.println(">>> [ASYNC-MAIL] ERROR CRITICO en hilo de bienvenida:");
            System.err.println(">>> Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(">>> Causa: " + e.getCause().getMessage());
            }
            // Logueamos el stack trace completo en Render para depuracion profunda
            e.printStackTrace();
        }
    }
}