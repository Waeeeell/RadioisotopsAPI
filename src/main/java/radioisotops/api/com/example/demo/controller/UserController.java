package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.service.EmailService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Usamos una ruta absoluta o relativa clara para evitar fallos en el despliegue
    private final Path storageLocation = Paths.get("uploads").toAbsolutePath().normalize().resolve("avatars");

    /**
     * 1. VISUALIZAR AVATAR
     * Ajustado para romper el bloqueo OpaqueResponseBlocking (ORB).
     */
    @GetMapping("/view-avatar/{filename:.+}")
    public ResponseEntity<Resource> verAvatar(@PathVariable String filename) {
        try {
            Path filePath = this.storageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) contentType = "image/png";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // Cabeceras vitales para el Proxy y el Navegador
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Cross-Origin-Resource-Policy", "cross-origin")
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 2. SUBIR AVATAR
     */
    @PostMapping("/{id}/upload-avatar")
    public ResponseEntity<?> subirAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));

            User user = userOpt.get();

            // Crear carpetas si no existen
            if (!Files.exists(this.storageLocation)) {
                Files.createDirectories(this.storageLocation);
            }

            // Nombre único para evitar colisiones y problemas de caché
            String extension = ".png";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = "avatar_" + id + "_" + System.currentTimeMillis() + extension;
            Path targetLocation = this.storageLocation.resolve(fileName);

            // Guardar con reemplazo por seguridad
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar URL en DB
            user.setProfilePicUrl("/api/users/view-avatar/" + fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Imagen actualizada",
                    "url", user.getProfilePicUrl()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 3. REGISTRO DE MÉDICOS
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            // Extraer email del administrador desde el token (inyectado por el filtro)
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
            }

            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
            }

            String passwordTemporal = nuevoUsuario.getPassword();
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");

            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            userRepository.save(nuevoUsuario);

            // Intento de envío de email (no bloquea la respuesta por ser @Async en el service)
            try {
                emailService.enviarBienvenidaMedico(
                        nuevoUsuario.getEmail(),
                        nuevoUsuario.getNombreCompleto(),
                        passwordTemporal
                );
            } catch (Exception ignored) {}

            return ResponseEntity.ok(Map.of("message", "Médico registrado correctamente"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}