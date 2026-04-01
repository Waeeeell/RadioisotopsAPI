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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/lista-gestion")
    public ResponseEntity<List<Map<String, Object>>> obtenerPacientesGestion() {
        List<Patient> pacientes = patientRepository.findAll();

        List<Map<String, Object>> respuesta = pacientes.stream().map(p -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("nombre", p.getUser() != null ? p.getUser().getNombreCompleto() : "Desconegut");
            dto.put("cip", p.getDni());

            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

            if (t != null && t.getRadioisotopo() != null) {
                double dosiInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosiInicial, t.getFechaInicio());

                dto.put("tratamiento", t.getRadioisotopo() + " (" + String.format("%.2f", activitatActual) + " MBq)");

                double progress = (activitatActual / dosiInicial) * 100;
                dto.put("progreso", (int) Math.round(progress));

                // Color segons normativa de l'Excel
                if (activitatActual > 400) dto.put("color", "red");
                else if (activitatActual > 1) dto.put("color", "yellow");
                else dto.put("color", "green");

            } else {
                dto.put("tratamiento", "Sense tractament");
                dto.put("progreso", 0);
                dto.put("color", "gray");
            }

            dto.put("estado", "ESTABLE");
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    private double calcularActivitatActual(String isotopo, double dosiInicial, LocalDateTime fechaInicio) {
        double teHores;

        // Assignem el Temps Efectiu (Te) segons el teu Excel
        if (isotopo.contains("I-131") || isotopo.contains("Iode")) {
            teHores = 16.4616; // 0.6859 dies * 24h
        } else if (isotopo.contains("Lu-177") || isotopo.contains("Lutenci")) {
            teHores = 16.176;  // 0.674 dies * 24h
        } else {
            return dosiInicial;
        }

        // t = temps transcorregut en hores
        long horesTranscorregudes = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();

        // Fórmula de l'Excel: A(t) = A0 * (0.5 ^ (t / Te))
        return dosiInicial * Math.pow(0.5, (double) horesTranscorregudes / teHores);
    }
}