package radioisotops.api.com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envío de nueva clave temporal tras un reset en el Panel de Auditoría.
     */
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
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Bienvenido al Portal Clinico - radioisotopo.portal");

            String contenido = "Estimado/a Dr./Dra. " + nombreMedico + ",\n\n" +
                    "Nos complace informarle de que su cuenta de acceso a la plataforma de monitorizacion nuclear ha sido creada correctamente. A continuacion, le facilitamos sus credenciales iniciales:\n\n" +
                    "• Usuario: " + emailDestino + "\n" +
                    "• Clave temporal: " + passwordTemporal + "\n\n" +
                    "Por motivos de seguridad y para garantizar la proteccion de los datos de nuestro hospital, le recomendamos cambiar su contrasena en su primer acceso.\n\n" +
                    "Puede acceder a la plataforma desde el siguiente enlace:\n" +
                    "https://radioisotopo.carriedo.cat\n\n" +
                    "Si necesita asistencia adicional o tiene cualquier consulta, estaremos encantados de ayudarle.\n\n" +
                    "Gracias por formar parte de radioisotopo.portal\n\n" +
                    "Un cordial saludo.";

            mensaje.setText(contenido);

            System.out.println(" Iniciando envio de bienvenida para: " + emailDestino);
            mailSender.send(mensaje);
            System.out.println(" Email de bienvenida enviado con exito.");

        } catch (MailException e) {
            System.err.println(" Fallo critico al enviar email de bienvenida:");
            e.printStackTrace();
            throw new RuntimeException("Error en el servidor de correo saliente");
        }
    }
}