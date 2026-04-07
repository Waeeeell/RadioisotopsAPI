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
     * Registro de médicos con envío de email asíncrono.
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            // 1. Verificación de autorización por token
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Sesión no válida o no autorizada"));
            }

            // 2. Comprobar si el correo ya está registrado para evitar duplicados
            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico ya está registrado en el sistema."));
            }

            // 3. Configurar metadatos del usuario
            String passwordTemporal = nuevoUsuario.getPassword();
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");

            // Vincular la entidad Doctor si viene en el cuerpo
            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            // 4. Persistencia en Base de Datos
            userRepository.save(nuevoUsuario);

            // 5. Envío de Email (Se ejecuta en segundo plano gracias a @Async en el Service)
            try {
                emailService.enviarBienvenidaMedico(
                        nuevoUsuario.getEmail(),
                        nuevoUsuario.getNombreCompleto(),
                        passwordTemporal
                );

                // Respondemos inmediatamente sin esperar a que el email termine de enviarse
                return ResponseEntity.ok(Map.of(
                        "message", "Médico registrado correctamente y proceso de email iniciado."
                ));

            } catch (Exception e) {
                // Caso poco probable con @Async, pero capturamos por seguridad
                return ResponseEntity.ok(Map.of(
                        "message", "Médico registrado, pero el servicio de mensajería no está disponible temporalmente."
                ));
            }

        } catch (Exception e) {
            // Logueamos el error en Render
            System.err.println("Error en registro de médico: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Fallo crítico en el servidor: " + e.getMessage()
            ));
        }
    }
}