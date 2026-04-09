package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.service.EmailService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS}
)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Directorio donde se guardarán las imágenes (Asegúrate de crearlo en tu proyecto)
    private final String UPLOAD_DIR = "uploads/avatars/";

    /**
     * Endpoint para subir o actualizar el avatar del usuario.
     */
    @PostMapping("/{id}/upload-avatar")
    public ResponseEntity<?> subirAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // 1. Validar si el usuario existe
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }
            User user = userOpt.get();

            // 2. Crear el directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3. Generar nombre de archivo único para evitar duplicados/caché
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String fileName = "avatar_" + id + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // 4. Guardar el archivo físicamente
            Files.copy(file.getInputStream(), filePath);

            // 5. Actualizar la ruta en la base de datos
            user.setProfilePicUrl("/api/users/view-avatar/" + fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Imagen de perfil actualizada correctamente",
                    "url", user.getProfilePicUrl()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al procesar la imagen: " + e.getMessage()));
        }
    }

    /**
     * Registro de médicos con envío de email asíncrono. (Tu código previo)
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Sesión no válida o no autorizada"));
            }

            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico ya está registrado en el sistema."));
            }

            String passwordTemporal = nuevoUsuario.getPassword();
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
                        passwordTemporal
                );
                return ResponseEntity.ok(Map.of("message", "Médico registrado correctamente y proceso de email iniciado."));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("message", "Médico registrado, pero el servicio de mensajería no está disponible."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fallo crítico en el servidor: " + e.getMessage()));
        }
    }
}