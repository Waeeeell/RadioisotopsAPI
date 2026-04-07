package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.service.EmailService;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
// Configuración robusta de CORS para que el Worker de Cloudflare no bloquee el POST
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.POST, RequestMethod.OPTIONS}
)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Registro de médicos con envío de email automático.
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            // 1. Verificación de autorización por token (inyectado por el Filter)
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            // 2. Comprobar si el correo ya está registrado
            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El email ya existe."));
            }

            // 3. Preparar datos del nuevo facultativo
            String passwordParaEmail = nuevoUsuario.getPassword();
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");

            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            // 4. Guardar en Base de Datos
            userRepository.save(nuevoUsuario);

            // 5. Envío de Email (Síncrono para mantener estabilidad de la conexión con el Proxy)
            try {
                emailService.enviarBienvenidaMedico(
                        nuevoUsuario.getEmail(),
                        nuevoUsuario.getNombreCompleto(),
                        passwordParaEmail
                );
                return ResponseEntity.ok(Map.of("message", "Médico registrado correctamente y email enviado."));
            } catch (Exception e) {
                // Si falla el email, no revertimos el registro, pero avisamos al Admin
                return ResponseEntity.ok(Map.of("message", "Médico registrado en sistema, pero hubo un error al enviar el email informativo."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al registrar médico: " + e.getMessage()));
        }
    }
}