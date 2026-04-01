package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDate;
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
                return ResponseEntity.badRequest().body("Error: Faltan datos en el envío.");
            }

            String doctorEmail = (String) request.getAttribute("userEmail");
            User usuarioMedico = userRepository.findByEmail(doctorEmail)
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            Doctor doc = usuarioMedico.getDoctor();

            String cip = (String) datosPaciente.get("cip");
            User userPaciente = new User();
            userPaciente.setNombreCompleto((String) datosPaciente.get("nombreCompleto"));
            userPaciente.setEmail(cip + "@catsalut.cat");
            userPaciente.setPassword(cip);
            userPaciente.setRol("PACIENTE");
            userPaciente.setEstado("ACTIVO");
            userPaciente.setFechaRegistro(LocalDateTime.now());

            userPaciente.setHospitalRef((String) datosPaciente.get("hospitalReferencia"));

            User userGuardado = userRepository.save(userPaciente);

            Patient patient = new Patient();
            patient.setUser(userGuardado);
            patient.setDni(cip);
            patient.setNumSs(cip);

            String fechaNacStr = (String) datosPaciente.get("fechaNacimiento");
            if (fechaNacStr != null && !fechaNacStr.isEmpty()) {
                patient.setFechaNacimiento(LocalDate.parse(fechaNacStr));
            }

            Patient patientGuardado = patientRepository.save(patient);

            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) datosTratamiento.get("radioisotopo"));

            Object dosisObj = datosTratamiento.get("dosis");
            if (dosisObj != null && !dosisObj.toString().isEmpty()) {
                treatment.setDosis(Double.valueOf(dosisObj.toString()));
            } else {
                treatment.setDosis(0.0);
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setDoctor(doc);
            treatment.setInstrucciones("Alta inicial: Monitorización Zero-Config activa.");

            treatmentRepository.save(treatment);

            return ResponseEntity.ok("Alta procesada con éxito por el Dr/a. " + usuarioMedico.getNombreCompleto());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el alta: " + e.getMessage());
        }
    }

    @GetMapping("/count-total")
    public ResponseEntity<Long> obtenerTotalPacientes() {
        long total = patientRepository.count();
        return ResponseEntity.ok(total);
    }
}