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

    // Directorio donde se guardarán las imágenes
    private final String UPLOAD_DIR = "uploads/avatars/";

    /**
     * 1. ENDPOINT PARA VISUALIZAR EL AVATAR
     * Soluciona el error NS_BINDING_ABORTED y OpaqueResponseBlocking.
     */
    @GetMapping("/view-avatar/{filename:.+}")
    public ResponseEntity<Resource> verAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Determinamos el tipo de contenido dinámicamente
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        // Cabecera fundamental para que el navegador acepte la imagen desde el proxy
                        .header("Cross-Origin-Resource-Policy", "cross-origin")
                        // Cacheamos un poco para mejorar rendimiento, pero permitimos cambios
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 2. ENDPOINT PARA SUBIR EL AVATAR
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

            // 3. Generar nombre de archivo único con timestamp
            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".png";

            String fileName = "avatar_" + id + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // 4. Guardar físicamente
            Files.copy(file.getInputStream(), filePath);

            // 5. Actualizar la ruta en la base de datos
            // Esta ruta coincide con nuestro @GetMapping de arriba
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
     * 3. REGISTRO DE MÉDICOS (Lógica previa)
     */
    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Sesión no válida o no autorizada"));
            }

            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico ya está registrado."));
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
                return ResponseEntity.ok(Map.of("message", "Médico registrado correctamente."));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("message", "Médico registrado, fallo envío email."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Fallo crítico: " + e.getMessage()));
        }
    }
}