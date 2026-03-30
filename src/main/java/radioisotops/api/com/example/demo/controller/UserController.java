package radioisotops.api.com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users") // Fíjate que cambiamos la ruta a /users
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register-doctor")
    public ResponseEntity<?> registrarDoctor(@RequestBody User nuevoUsuario) {
        try {
            if (userRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El email ya existe.");
            }

            nuevoUsuario.setFechaRegistro(LocalDateTime.now());
            nuevoUsuario.setRol("MEDICO");
            
            if (nuevoUsuario.getDoctor() != null) {
                nuevoUsuario.getDoctor().setUser(nuevoUsuario);
            }

            userRepository.save(nuevoUsuario);
            return ResponseEntity.ok("Médico creado correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor: " + e.getMessage());
        }
    }
}