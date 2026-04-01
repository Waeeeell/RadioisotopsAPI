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

            // Registro de Usuario y Paciente (Tu lógica original)
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

            // --- LÓGICA DE TRATAMIENTO CON CONVERSIÓN ---
            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) datosTratamiento.get("radioisotopo"));

            Object dosisObj = datosTratamiento.get("dosis");
            String unidad = (String) datosTratamiento.get("unidad"); // Capturamos la unidad del Frontend

            if (dosisObj != null && !dosisObj.toString().isEmpty()) {
                double valorDosis = Double.parseDouble(dosisObj.toString());
                double dosisEnMBq;

                // Aplicamos conversión según tu Excel: 1 mCi = 37 MBq
                if ("mCi".equalsIgnoreCase(unidad)) {
                    dosisEnMBq = valorDosis * 37.0;
                } else if ("Ci".equalsIgnoreCase(unidad)) {
                    dosisEnMBq = valorDosis * 37000.0;
                } else {
                    dosisEnMBq = valorDosis; // Por defecto MBq
                }
                treatment.setDosis(dosisEnMBq);
            } else {
                treatment.setDosis(0.0);
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setDoctor(doc);
            treatment.setInstrucciones("Alta inicial: Monitorización activa.");

            treatmentRepository.save(treatment);

            return ResponseEntity.ok("Alta procesada con éxito por el Dr/a. " + usuarioMedico.getNombreCompleto());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error en el alta: " + e.getMessage());
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

                // Mostramos el valor actual calculado
                dto.put("tratamiento", t.getRadioisotopo() + " (" + String.format("%.2f", activitatActual) + " MBq)");

                // El progreso es el % de actividad que queda respecto a la inicial
                double progress = (activitatActual / dosiInicial) * 100;
                dto.put("progreso", (int) Math.round(progress));

                // Semáforo clínico del Excel
                if (activitatActual > 400) {
                    dto.put("color", "red");
                    dto.put("estado", "CRÍTIC");
                } else if (activitatActual > 1) {
                    dto.put("color", "yellow");
                    dto.put("estado", "ESTABLE");
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
        double teHores;
        if (isotopo.contains("I-131") || isotopo.contains("Iode")) {
            teHores = 16.4616; // 0.6859 dies * 24h
        } else if (isotopo.contains("Lu-177") || isotopo.contains("Lutenci")) {
            teHores = 16.176;  // 0.674 dies * 24h
        } else {
            return dosiInicial;
        }

        long hores = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();
        return dosiInicial * Math.pow(0.5, (double) hores / teHores);
    }
}