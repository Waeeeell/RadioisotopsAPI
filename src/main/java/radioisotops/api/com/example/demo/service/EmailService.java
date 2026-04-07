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

    public void enviarPasswordTemporal(String emailDestino, String nombreMedico, String nuevaPassword) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Restablecimiento de acceso - Portal Radioisotopos");
            mensaje.setText("Hola, Dr./Dra. " + nombreMedico + ",\n\n" +
                    "Se ha generado una nueva contrasena temporal para su acceso al sistema de monitorizacion nuclear.\n\n" +
                    "Nueva contrasena: " + nuevaPassword + "\n\n" +
                    "Por favor, inicie sesion y cambie su contrasena desde el panel de configuracion lo antes posible.\n\n" +
                    "Saludos,\n" +
                    "Departamento de Seguridad IT");

            System.out.println("========== ENVIANDO EMAIL ==========");
            System.out.println("Para: " + emailDestino);
            System.out.println("Desde: radioisotopo.portal@gmail.com");
            System.out.println("====================================");
            
            mailSender.send(mensaje);
            System.out.println("EMAIL ENVIADO EXITOSAMENTE a: " + emailDestino);
        } catch (MailException e) {
            System.err.println("ERROR AL ENVIAR EMAIL:");
            e.printStackTrace();
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom("radioisotopo.portal@gmail.com");
            mensaje.setTo(emailDestino);
            mensaje.setSubject("Bienvenido al Portal Clinico de Radioisotopos");
            mensaje.setText("Estimado/a Dr./Dra. " + nombreMedico + ",\n\n" +
                    "Le damos la bienvenida a la plataforma de monitorizacion nuclear. " +
                    "Se ha creado su perfil de facultativo correctamente.\n\n" +
                    "Sus credenciales de acceso son:\n" +
                    "Usuario: " + emailDestino + "\n" +
                    "Contrasena temporal: " + passwordTemporal + "\n\n" +
                    "Puede acceder al portal desde la direccion de red del hospital.\n" +
                    "Por motivos de seguridad, cambie su contrasena tras el primer inicio de sesion.\n\n" +
                    "Atentamente,\n" +
                    "Administracion del Centro");

            System.out.println("========== ENVIANDO EMAIL ==========");
            System.out.println("Para: " + emailDestino);
            System.out.println("Desde: radioisotopo.portal@gmail.com");
            System.out.println("====================================");
            
            mailSender.send(mensaje);
            System.out.println("EMAIL ENVIADO EXITOSAMENTE a: " + emailDestino);
        } catch (MailException e) {
            System.err.println("ERROR AL ENVIAR EMAIL:");
            e.printStackTrace();
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }
}
