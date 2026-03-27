package radioisotops.api.com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.LoginRequest;
import radioisotops.api.com.example.demo.dto.LoginResponseDTO;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Esto evita que React Native bloquee la conexión
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginRequest loginRequest) {

        Optional<User> usuarioOpt = userRepository.findByEmail(loginRequest.getEmail());

        if (usuarioOpt.isPresent()) {
            User usuario = usuarioOpt.get();

            if (usuario.getContraseña().equals(loginRequest.getContraseña())) {

                // Preparamos los datos base
                String especialidad = null;
                String colegiado = null;

                // Si es médico, buscamos sus datos extra a través de la relación
                if ("MEDICO".equals(usuario.getRol()) && usuario.getDoctor() != null) {
                    especialidad = usuario.getDoctor().getEspecialidad();
                    colegiado = usuario.getDoctor().getColegiadoNum();
                }

                // Creamos el paquete completo para React
                LoginResponseDTO respuesta = new LoginResponseDTO(
                        usuario.getEmail(),
                        usuario.getNombreCompleto(),
                        usuario.getRol(),
                        especialidad,
                        colegiado);

                return ResponseEntity.ok(respuesta);
            }
        }
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }
}