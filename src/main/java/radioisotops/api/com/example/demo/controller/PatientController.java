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
            String unidad = (String) datosTratamiento.get("unidad");

            if (dosisObj != null && !dosisObj.toString().isEmpty()) {
                double valorDosis = Double.parseDouble(dosisObj.toString());
                double dosisEnMBq;

                // Conversión 1 mCi = 37 MBq según Excel
                if ("mCi".equalsIgnoreCase(unidad)) {
                    dosisEnMBq = valorDosis * 37.0;
                } else if ("Ci".equalsIgnoreCase(unidad)) {
                    dosisEnMBq = valorDosis * 37000.0;
                } else {
                    dosisEnMBq = valorDosis;
                }
                treatment.setDosis(dosisEnMBq);
            } else {
                treatment.setDosis(0.0);
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setDoctor(doc);
            treatment.setInstrucciones("Monitorització activa basada en decaïment físic real.");

            treatmentRepository.save(treatment);

            return ResponseEntity.ok("Alta processada amb èxit pel Dr/a. " + usuarioMedico.getNombreCompleto());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el alta: " + e.getMessage());
        }
    }

    @GetMapping("/count-total")
    public ResponseEntity<Long> obtenerTotalPacientes() {
        try {
            return ResponseEntity.ok(patientRepository.count());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(0L);
        }
    }

    @GetMapping("/lista-gestion")
    public ResponseEntity<List<Map<String, Object>>> obtenerPacientesGestion() {
        List<Patient> pacientes = patientRepository.findAll();

        return ResponseEntity.ok(pacientes.stream().map(p -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("nombre", p.getUser() != null ? p.getUser().getNombreCompleto() : "Desconegut");
            dto.put("cip", p.getDni());

            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

            if (t != null && t.getRadioisotopo() != null) {
                double dosiInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosiInicial, t.getFechaInicio());

                String isoOriginal = t.getRadioisotopo();
                String isoBonito = isoOriginal;
                if (isoOriginal.contains("I-131")) isoBonito = "Iodo-131";
                else if (isoOriginal.contains("Lu-177")) isoBonito = "Luteci-177";
                else if (isoOriginal.contains("Co-60")) isoBonito = "Cobalt-60";

                dto.put("tratamiento", isoBonito + " (" + String.format("%.2f", activitatActual) + " MBq)");

                // El progreso es la actividad que queda respecto a la inicial
                double progress = (activitatActual / dosiInicial) * 100;
                dto.put("progreso", (int) Math.round(progress));

                // --- UMBRALES REALES EXCEL DHM ---
                if (activitatActual > 400) {
                    dto.put("color", "red");
                    dto.put("estado", "INGRESSAT");
                } else if (activitatActual > 1) {
                    dto.put("color", "yellow");
                    dto.put("estado", "AMBULATORI");
                } else {
                    dto.put("color", "green");
                    dto.put("estado", "EXEMPT");
                }
            } else {
                dto.put("tratamiento", "Sense tractament");
                dto.put("progreso", 0);
                dto.put("color", "gray");
                dto.put("estado", "PENDENT");
            }
            return dto;
        }).collect(Collectors.toList()));
    }

    private double calcularActivitatActual(String isotopo, double dosiInicial, LocalDateTime fechaInicio) {
        double tMedHores;

        // Valores extraídos de tu tabla de decaimiento
        if (isotopo.contains("I-131") || isotopo.contains("Iodo")) {
            tMedHores = 192.48; // 8.02 días * 24h
        } else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) {
            tMedHores = 159.36; // 6.64 días * 24h
        } else if (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) {
            tMedHores = 46164.0; // 5.27 años * 365 días * 24h
        }  else {
            return dosiInicial;
        }

        // t = tiempo transcurrido desde la administración
        long horesTranscorregudes = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();

        // Fórmula exponencial: A(t) = A0 * 0.5^(t/T1/2)
        return dosiInicial * Math.pow(0.5, (double) horesTranscorregudes / tMedHores);
    }
}