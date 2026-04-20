package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.service.EmailService;

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

    /**
     * 1. SUBIDA DE AVATAR A CLOUDINARY
     * Persiste la imagen en el CDN y actualiza la URL en la base de datos.
     */
    @PostMapping("/{id}/upload-avatar")
    public ResponseEntity<?> subirAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
            }
            User user = userOpt.get();

            // Subir a la nube usando un ID público basado en el usuario para evitar duplicados
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", "avatar_user_" + id,
                    "folder", "avatars",
                    "overwrite", true,
                    "resource_type", "image"
            ));

            String url = (String) uploadResult.get("secure_url");

            // Guardar la URL permanente en la DB
            user.setProfilePicUrl(url);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Imagen actualizada en Cloudinary",
                    "url", url
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al subir al CDN: " + e.getMessage()));
        }
    }

    /**
     * 2. REGISTRO DE MÉDICOS
     * Crea el usuario, marca el cambio de password obligatorio y envía email.
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

            // Configuración inicial de seguridad
            String passwordTemporal = nuevoUsuario.getPassword();
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");
            nuevoUsuario.setRequiereCambioPassword(true); // Obliga a cambiar clave en el primer login

            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            userRepository.save(nuevoUsuario);

            // Envío de credenciales por email (Asíncrono)
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

    /**
     * 3. ACTUALIZAR CONTRASEÑA (Paso final del flujo de activación)
     * Cambia la clave temporal por una definitiva y libera el acceso.
     */
    @PutMapping("/{id}/update-password")
    @Transactional
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return userRepository.findById(id).map(user -> {
            String nuevaPassword = payload.get("password");

            if (nuevaPassword == null || nuevaPassword.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "Contraseña demasiado corta"));
            }

            // Actualizamos clave y quitamos el flag de bloqueo
            user.setPassword(nuevaPassword);
            user.setRequiereCambioPassword(false);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 4. OBTENER PERFIL DE USUARIO
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}