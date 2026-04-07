package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.service.EmailService; // Importamos el servicio
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService; // Inyectamos el servicio de correo

    /**
     * Registro de médicos con envío de email automático.
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) return ResponseEntity.status(401).body("No autorizado");

            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El email ya existe.");
            }

            String passwordParaEmail = nuevoUsuario.getPassword();

            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");

            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            userRepository.save(nuevoUsuario);

            try {
                emailService.enviarBienvenidaMedico(
                        nuevoUsuario.getEmail(),
                        nuevoUsuario.getNombreCompleto(),
                        passwordParaEmail
                );
                return ResponseEntity.ok("Médico registrado correctamente y email enviado.");
            } catch (Exception e) {
                return ResponseEntity.ok("Médico registrado, pero hubo un error al enviar el email: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al registrar médico: " + e.getMessage());
        }
    }
}