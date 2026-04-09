package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.LoginRequest;
import radioisotops.api.com.example.demo.dto.LoginResponseDTO;
import radioisotops.api.com.example.demo.dto.PreferenciasDTO;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.security.JwtUtil;
import radioisotops.api.com.example.demo.service.EmailService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
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
                boolean temporal = usuario.getPassword().startsWith("Temp");

                LoginResponseDTO respuesta = new LoginResponseDTO(
                        usuario.getId(),
                        usuario.getEmail(),
                        usuario.getNombreCompleto(),
                        usuario.getRol(),
                        especialidad,
                        colegiado,
                        token,
                        temporal,
                        usuario.getIdioma(),
                        usuario.getZonaHoraria(),
                        usuario.getNotifBateria(),
                        usuario.getNotifDesconexion(),
                        usuario.getNotifResumen(),
                        usuario.getNotifRadiacion(),
                        usuario.getNotifVitales(),
                        usuario.getNotifSincro(),
                        usuario.getProfilePicUrl() // ✅ NUEVO
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
                    boolean temporal = user.getPassword().startsWith("Temp");

                    return ResponseEntity.ok(new LoginResponseDTO(
                            user.getId(),
                            user.getEmail(),
                            user.getNombreCompleto(),
                            user.getRol(),
                            especialidad,
                            colegiado,
                            null,
                            temporal,
                            user.getIdioma(),
                            user.getZonaHoraria(),
                            user.getNotifBateria(),
                            user.getNotifDesconexion(),
                            user.getNotifResumen(),
                            user.getNotifRadiacion(),
                            user.getNotifVitales(),
                            user.getNotifSincro(),
                            user.getProfilePicUrl() // ✅ NUEVO
                    ));
                })
                .orElse(ResponseEntity.status(404).build());
    }

    @GetMapping("/doctores")
    public ResponseEntity<List<User>> listarDoctores() {
        return ResponseEntity.ok(userRepository.findByRol("MEDICO"));
    }

    @PostMapping("/doctor/{id}/status")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            user.setEstado(body.get("estado"));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
        }).orElse(ResponseEntity.notFound().build());
    }

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

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        String email = (String) request.getAttribute("userEmail");
        String oldPass = body.get("oldPassword");
        String newPass = body.get("newPassword");

        return userRepository.findByEmail(email).map(user -> {
            if (!user.getPassword().equals(oldPass)) {
                return ResponseEntity.status(401).body(Map.of("message", "La contraseña actual es incorrecta"));
            }
            user.setPassword(newPass);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada con éxito"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/preferencias")
    public ResponseEntity<?> actualizarPreferencias(HttpServletRequest request, @RequestBody PreferenciasDTO dto) {
        String email = (String) request.getAttribute("userEmail");

        return userRepository.findByEmail(email).map(user -> {
            user.setIdioma(dto.idioma());
            user.setZonaHoraria(dto.zonaHoraria());
            user.setNotifBateria(dto.bateriaBaja());
            user.setNotifDesconexion(dto.desconexionBiometrica());
            user.setNotifResumen(dto.resumenSemanal());
            user.setNotifRadiacion(dto.radiacionSegura());
            user.setNotifVitales(dto.anomaliaVitales());
            user.setNotifSincro(dto.falloSincronizacion());

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Preferencias actualizadas"));
        }).orElse(ResponseEntity.notFound().build());
    }
}