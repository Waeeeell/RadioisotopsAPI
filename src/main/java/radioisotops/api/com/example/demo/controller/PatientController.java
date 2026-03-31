package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    /**
     * Endpoint protegido por JWT para realizar el alta completa.
     * La identidad del médico se extrae del Token de seguridad.
     */
    @PostMapping("/register-full")
    @Transactional
    public ResponseEntity<?> registrarAltaCompleta(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            String doctorEmail = (String) request.getAttribute("userEmail");

            if (doctorEmail == null) {
                return ResponseEntity.status(401).body("Error: No se encontró una sesión válida (Falta Token)");
            }

            User usuarioMedico = userRepository.findByEmail(doctorEmail)
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado en el sistema"));

            Doctor doc = usuarioMedico.getDoctor();
            if (doc == null) {
                return ResponseEntity.badRequest().body("Error: El usuario autenticado no tiene perfil de médico.");
            }

            User userPaciente = new User();
            userPaciente.setNombreCompleto((String) payload.get("nombreCompleto"));
            userPaciente.setEmail(payload.get("cip") + "@catsalut.cat");
            userPaciente.setPassword((String) payload.get("cip"));
            userPaciente.setRol("PACIENTE");
            userPaciente.setEstado("ACTIVO");
            userPaciente.setFechaRegistro(LocalDateTime.now());
            User userGuardado = userRepository.save(userPaciente);

            Patient patient = new Patient();
            patient.setUser(userGuardado);
            patient.setDni((String) payload.get("cip"));
            patient.setNumSs((String) payload.get("cip"));
            Patient patientGuardado = patientRepository.save(patient);

            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) payload.get("tipoIsotopo"));

            try {
                Object dosisObj = payload.get("dosis");
                if (dosisObj != null && !dosisObj.toString().trim().isEmpty()) {
                    String dosisLimpia = dosisObj.toString().replace(",", ".");
                    treatment.setDosis(Double.valueOf(dosisLimpia));
                } else {
                    treatment.setDosis(Double.valueOf(0.0));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Error: El formato de la dosis no es válido.");
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setInstrucciones("Alta inicial: Monitorización Zero-Config activa.");

            treatment.setDoctor(doc);

            treatmentRepository.save(treatment);

            return ResponseEntity.ok("Alta procesada correctamente por el Dr/a. " + usuarioMedico.getNombreCompleto());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el proceso de alta: " + e.getMessage());
        }
    }
}