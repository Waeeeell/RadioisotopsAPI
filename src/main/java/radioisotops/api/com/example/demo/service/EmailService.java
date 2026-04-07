package radioisotops.api.com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void enviarPasswordTemporal(String emailDestino, String nombreMedico, String nuevaPassword) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(emailDestino);
        mensaje.setSubject("Restablecimiento de acceso - Portal Radioisótopos");

        String contenido = "Hola, Dr./Dra. " + nombreMedico + ",\n\n" +
                "Se ha generado una nueva contraseña temporal para su acceso al sistema de monitorización nuclear.\n\n" +
                "Nueva contraseña: " + nuevaPassword + "\n\n" +
                "Por favor, inicie sesión y cambie su contraseña desde el panel de configuración lo antes posible.\n\n" +
                "Saludos,\n" +
                "Departamento de Seguridad IT";

        mensaje.setText(contenido);

        mailSender.send(mensaje);
        System.out.println("📧 Correo enviado con éxito a: " + emailDestino);
    }

    @Async
    public void enviarBienvenidaMedico(String emailDestino, String nombreMedico, String passwordTemporal) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(emailDestino);
        mensaje.setSubject("Bienvenido al Portal Clínico de Radioisótopos");

        String contenido = "Estimado/a Dr./Dra. " + nombreMedico + ",\n\n" +
                "Le damos la bienvenida a la plataforma de monitorización nuclear. " +
                "Se ha creado su perfil de facultativo correctamente.\n\n" +
                "Sus credenciales de acceso son:\n" +
                "Usuario: " + emailDestino + "\n" +
                "Contraseña temporal: " + passwordTemporal + "\n\n" +
                "Puede acceder al portal desde la dirección de red del hospital.\n" +
                "Por motivos de seguridad, cambie su contraseña tras el primer inicio de sesión.\n\n" +
                "Atentamente,\n" +
                "Administración del Centro";

        mensaje.setText(contenido);
        mailSender.send(mensaje);
        System.out.println("📧 Email de bienvenida enviado a: " + emailDestino);
    }
}