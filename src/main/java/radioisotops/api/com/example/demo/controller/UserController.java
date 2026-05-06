/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de User Controller]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
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

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", "avatar_user_" + id,
                    "folder", "avatars",
                    "overwrite", true,
                    "resource_type", "image"
            ));

            String url = (String) uploadResult.get("secure_url");
            user.setProfilePicUrl(url);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Imagen actualizada", "url", url));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario, HttpServletRequest request) {
        try {
            String adminEmail = (String) request.getAttribute("userEmail");
            if (adminEmail == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado para registrar médicos"));
            }

            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email ya registrado"));
            }

            String passwordTemporal = nuevoUsuario.getPassword();
            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            nuevoUsuario.setEstado("ACTIVO");
            nuevoUsuario.setRequiereCambioPassword(true);

            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            userRepository.save(nuevoUsuario);

            try {
                emailService.enviarBienvenidaMedico(nuevoUsuario.getEmail(), nuevoUsuario.getNombreCompleto(), passwordTemporal);
            } catch (Exception ignored) {}

            return ResponseEntity.ok(Map.of("message", "Médico registrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/update-password")
    @Transactional
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return userRepository.findById(id).map(user -> {
            String nuevaPassword = payload.get("password");

            if (nuevaPassword == null || nuevaPassword.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "Contraseña demasiado corta"));
            }

            user.setPassword(nuevaPassword);
            user.setRequiereCambioPassword(false);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfil(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}