package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.LoginRequest;
import radioisotops.api.com.example.demo.dto.LoginResponseDTO;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.security.JwtUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Esto evita que React Native bloquee la conexión
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest loginRequest) {

        Optional<User> usuarioOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            User usuario = usuarioOpt.get();

            if (usuario.getPassword().equals(loginRequest.getPassword())) {

                // Preparamos los datos base
                String especialidad = null;
                String colegiado = null;

                // Si es médico, buscamos sus datos extra a través de la relación
                if ("MEDICO".equals(usuario.getRol()) && usuario.getDoctor() != null) {
                    especialidad = usuario.getDoctor().getEspecialidad();
                    colegiado = usuario.getDoctor().getColegiadoNum();
                }

                String token = jwtUtil.generateToken(usuario.getEmail());

                // Creamos el paquete completo para React
                LoginResponseDTO respuesta = new LoginResponseDTO(
                        usuario.getId(),
                        usuario.getEmail(),
                        usuario.getNombreCompleto(),
                        usuario.getRol(),
                        especialidad,
                        colegiado,
                        token
                );

                return ResponseEntity.ok(respuesta);
            }
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");

        if (email == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    String especialidad = (user.getDoctor() != null) ? user.getDoctor().getEspecialidad() : null;
                    String colegiado = (user.getDoctor() != null) ? user.getDoctor().getColegiadoNum() : null;

                    return ResponseEntity.ok(new LoginResponseDTO(
                            user.getId(),
                            user.getEmail(),
                            user.getNombreCompleto(),
                            user.getRol(),
                            especialidad,
                            colegiado,
                            null
                    ));
                })
                .orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/doctores")
    public ResponseEntity<List<User>> listarDoctores() {
        return ResponseEntity.ok(userRepository.findByRol("MEDICO"));
    }

    @PatchMapping("/doctor/{id}/status")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            user.setEstado(body.get("estado")); // "ACTIVO" o "INACTIVO"
            userRepository.save(user);
            return ResponseEntity.ok("Estado actualizado");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/doctor/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            user.setPassword(body.get("password")); // Aquí deberías usar BCrypt
            userRepository.save(user);
            return ResponseEntity.ok("Contraseña reseteada");
        }).orElse(ResponseEntity.notFound().build());
    }
}