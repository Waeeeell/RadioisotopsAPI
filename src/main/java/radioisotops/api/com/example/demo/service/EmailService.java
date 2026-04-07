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

            System.out.println("Iniciando envio de reset de password para: " + emailDestino);
            mailSender.send(mensaje);
            System.out.println("Email de reset enviado con exito.");

        } catch (MailException e) {
            System.err.println("Fallo critico al enviar email de reset:");
            e.printStackTrace();
            // Lanzamos excepcion para que el controlador pueda informar del error
            throw new RuntimeException("No se pudo conectar con el servidor de correo");
        }
    }

    /**
     * Envío de bienvenida y credenciales para nuevos médicos registrados.
     */
    @Async
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        try {
            System.out.println(">>> [MAIL] Preparando mensaje para: " + emailDestino);

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Bienvenido al Portal Clinico");
            mensaje.setText("Hola Dr. " + nombreMedico + ". Su clave es: " + passwordTemporal);

            // LOG DE SEGURIDAD (Para verificar que las variables de entorno llegan bien)
            // No imprimas la clave real por seguridad, solo confirma que no es nula
            System.out.println(">>> [MAIL] Intentando conectar con smtp.gmail.com:587");

            mailSender.send(mensaje);

            System.out.println(">>> [MAIL] ¡EXITO! El servidor de Google ha aceptado el correo.");

        } catch (Exception e) {
            System.err.println(">>> [MAIL] ERROR CRITICO detectado:");
            System.err.println(">>> Clase del error: " + e.getClass().getName());
            System.err.println(">>> Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(">>> Causa original: " + e.getCause().getMessage());
            }
            // IMPORTANTE: Lanzamos la excepción para que el controlador la vea
            throw new RuntimeException("Error en envio SMTP: " + e.getMessage());
        }
    }
}