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
            Map<String, Object> datosPaciente = (Map<String, Object>) payload.get("paciente");
            Map<String, Object> datosTratamiento = (Map<String, Object>) payload.get("tratamiento");

            if (datosPaciente == null || datosTratamiento == null) {
                return ResponseEntity.badRequest().body("Error: Estructura de datos incompleta.");
            }

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
            userPaciente.setNombreCompleto((String) datosPaciente.get("nombreCompleto"));
            userPaciente.setEmail(datosPaciente.get("cip") + "@catsalut.cat");
            userPaciente.setPassword((String) datosPaciente.get("cip")); // Password por defecto es su CIP
            userPaciente.setRol("PACIENTE");
            userPaciente.setEstado("ACTIVO");
            userPaciente.setFechaRegistro(LocalDateTime.now());
            User userGuardado = userRepository.save(userPaciente);

            Patient patient = new Patient();
            patient.setUser(userGuardado);
            patient.setDni((String) datosPaciente.get("cip"));
            patient.setNumSs((String) datosPaciente.get("cip"));

            Patient patientGuardado = patientRepository.save(patient);

            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) datosTratamiento.get("radioisotopo"));

            try {
                Object dosisObj = datosTratamiento.get("dosis");
                if (dosisObj != null && !dosisObj.toString().trim().isEmpty()) {
                    String dosisLimpia = dosisObj.toString().replace(",", ".");
                    treatment.setDosis(Double.valueOf(dosisLimpia));
                } else {
                    treatment.setDosis(0.0);
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Error: El formato de la dosis no es válido.");
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setDoctor(doc);
            treatment.setInstrucciones("Alta inicial: Monitorización Zero-Config activa.");

            treatmentRepository.save(treatment);

            return ResponseEntity.ok("Alta procesada correctamente por el Dr/a. " + usuarioMedico.getNombreCompleto());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el proceso de alta: " + e.getMessage());
        }
    }
}