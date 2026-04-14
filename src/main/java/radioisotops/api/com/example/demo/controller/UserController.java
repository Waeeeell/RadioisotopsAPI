package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Transformation;
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

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/{id}/upload-avatar")
    public ResponseEntity<?> subirAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            User user = userOpt.get();

            // 1. Subir a la nube usando el ID para que siempre se sobrescriba la misma foto
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", "avatar_user_" + id,
                    "folder", "avatars",
                    "overwrite", true,
                    "resource_type", "image"
            ));

            // 2. Extraer la URL permanente
            String url = (String) uploadResult.get("secure_url");

            // 3. Guardar en la DB (Ahora apuntará a Cloudinary, no a tu disco duro)
            user.setProfilePicUrl(url);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Imagen persistente en Cloudinary", "url", url));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al subir al CDN: " + e.getMessage()));
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