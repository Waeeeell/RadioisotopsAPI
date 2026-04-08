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
import radioisotops.api.com.example.demo.service.EmailService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
// Añadimos PUT y PATCH a la lista de permitidos por si acaso, aunque usemos POST
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS}
)
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    /**
     * LOGIN DE USUARIOS
     */
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest loginRequest) {
        Optional<User> usuarioOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            User usuario = usuarioOpt.get();

            if (usuario.getPassword().equals(loginRequest.getPassword())) {
                String especialidad = null;
                String colegiado = null;

                if ("MEDICO".equals(usuario.getRol()) && usuario.getDoctor() != null) {
                    especialidad = usuario.getDoctor().getEspecialidad();
                    colegiado = usuario.getDoctor().getColegiadoNum();
                }

                String token = jwtUtil.generateToken(usuario.getEmail());

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

    /**
     * OBTENER DATOS DEL USUARIO LOGUEADO
     */
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

    /**
     * LISTADO DE MÉDICOS PARA EL PANEL DE AUDITORÍA
     */
    @GetMapping("/doctores")
    public ResponseEntity<List<User>> listarDoctores() {
        return ResponseEntity.ok(userRepository.findByRol("MEDICO"));
    }

    /**
     * CAMBIO DE ESTADO (SUSPENDER/ACTIVAR)
     */
    @PostMapping("/doctor/{id}/status")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            user.setEstado(body.get("estado"));
            userRepository.save(user);
            return ResponseEntity.ok("Estado actualizado correctamente");
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * RESET DE CONTRASEÑA
     * IMPORTANTE: Asegúrate de que el frontend use method: 'POST'
     */
    @PostMapping("/doctor/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            String nuevaPass = body.get("password");

            user.setPassword(nuevaPass);
            userRepository.save(user);

            try {
                emailService.enviarPasswordTemporal(user.getEmail(), user.getNombreCompleto(), nuevaPass);
                return ResponseEntity.ok(Map.of("message", "Contrasena actualizada y correo enviado."));
            } catch (Exception e) {
                return ResponseEntity.ok(Map.of("message", "Contrasena actualizada, pero el correo fallo."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}