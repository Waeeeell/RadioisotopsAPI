package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.WatchEstadoDTO; // <-- Importante para el reloj
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;
import radioisotops.api.com.example.demo.service.EmailService;
import radioisotops.api.com.example.demo.service.PdfGeneratorService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class PatientController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PdfGeneratorService pdfService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EmailService emailService;

    /**
     * REGISTRO INTEGRAL DE PACIENTE Y TRATAMIENTO
     * Modificado para persistir el watchId del Bluetooth.
     */
    @PostMapping("/register-full")
    @Transactional
    public ResponseEntity<?> registrarAltaCompleta(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            Map<String, Object> datosPaciente = (Map<String, Object>) payload.get("paciente");
            Map<String, Object> datosTratamiento = (Map<String, Object>) payload.get("tratamiento");

            if (datosPaciente == null || datosTratamiento == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos en el envío."));
            }

            String doctorEmail = (String) request.getAttribute("userEmail");
            User usuarioMedico = userRepository.findByEmail(doctorEmail)
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            Doctor doc = usuarioMedico.getDoctor();
            String cip = (String) datosPaciente.get("cip");

            if (cip == null || cip.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El CIP es obligatorio."));
            }

            // 1. Crear Usuario
            User userPaciente = new User();
            userPaciente.setNombreCompleto((String) datosPaciente.get("nombreCompleto"));
            userPaciente.setEmail(cip.toLowerCase() + "@catsalut.cat");
            String passTemporal = "Temp" + (cip.length() >= 4 ? cip.substring(0, 4) : cip);
            userPaciente.setPassword(passTemporal);
            userPaciente.setRol("PACIENTE");
            userPaciente.setEstado("ACTIVO");
            userPaciente.setRequiereCambioPassword(true);
            userPaciente.setFechaRegistro(LocalDateTime.now());
            userPaciente.setHospitalRef((String) datosPaciente.get("hospitalReferencia"));
            User userGuardado = userRepository.save(userPaciente);

            // 2. Crear Perfil de Paciente
            Patient patient = new Patient();
            patient.setUser(userGuardado);
            patient.setDni(cip);
            patient.setNumSs(cip);

            // MODIFICACIÓN: Guardar el watchId que viene del payload
            if (datosPaciente.containsKey("watchId")) {
                patient.setWatchId((String) datosPaciente.get("watchId"));
            }

            String fechaNacStr = (String) datosPaciente.get("fechaNacimiento");
            if (fechaNacStr != null && !fechaNacStr.isEmpty()) {
                patient.setFechaNacimiento(LocalDate.parse(fechaNacStr));
            }

            patient.setDoctorAsignado(doc);
            Patient patientGuardado = patientRepository.save(patient);

            // 3. Crear Tratamiento Inicial
            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) datosTratamiento.get("radioisotopo"));

            Object dosisObj = datosTratamiento.get("dosis");
            String unidad = (String) datosTratamiento.get("unidad");

            if (dosisObj != null && !dosisObj.toString().isEmpty()) {
                double valorDosis = Double.parseDouble(dosisObj.toString());
                double dosisEnMBq = "mCi".equalsIgnoreCase(unidad) ? valorDosis * 37.0 :
                        "Ci".equalsIgnoreCase(unidad) ? valorDosis * 37000.0 : valorDosis;
                treatment.setDosis(dosisEnMBq);
            } else {
                treatment.setDosis(0.0);
            }

            treatment.setFechaInicio(LocalDateTime.now());
            treatment.setPatient(patientGuardado);
            treatment.setDoctor(doc);
            treatment.setInstrucciones("Monitorización activa basada en decaimiento físico real.");
            treatmentRepository.save(treatment);

            return ResponseEntity.ok(Map.of(
                    "message", "Alta procesada con éxito por el Dr/a. " + usuarioMedico.getNombreCompleto(),
                    "accesoEmail", userPaciente.getEmail(),
                    "accesoPasswordTemporal", passTemporal,
                    "requiereCambio", true
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error en el alta: " + e.getMessage()));
        }
    }

    /**
     * NUEVO: Obtener detalle completo del paciente para la página de perfil
     * Incluye datos dinámicos del reloj y estado del tratamiento.
     */
    @GetMapping("/perfil/{cip}")
    public ResponseEntity<?> obtenerPerfilPaciente(@PathVariable String cip) {
        return patientRepository.findByDni(cip).map(p -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("nombre", p.getUser().getNombreCompleto());
            dto.put("cip", p.getDni());
            dto.put("valorEmocional", p.getValorEmocional());

            // Datos del Smartwatch (CORREGIDO NULL SAFETY)
            dto.put("watchId", p.getWatchId());
            dto.put("watchBattery", p.getWatchBattery() != null ? p.getWatchBattery() : 0);
            dto.put("watchUltimaSinc", p.getWatchUltimaSinc());
            dto.put("watchModel", p.getWatchModel() != null ? p.getWatchModel() : "Galaxy Watch");

            // Datos del Tratamiento y Progreso
            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);
            if (t != null && t.getRadioisotopo() != null) {
                double dosiInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosiInicial, t.getFechaInicio());

                dto.put("tratamiento", t.getRadioisotopo() + " (" + String.format("%.2f", activitatActual) + " MBq)");
                double progress = (dosiInicial > 0) ? (activitatActual / dosiInicial) * 100 : 0;
                dto.put("progreso", (int) Math.round(progress));

                if (activitatActual > 400) { dto.put("color", "red"); dto.put("estado", "Fase Inicial"); }
                else if (activitatActual > 1) { dto.put("color", "yellow"); dto.put("estado", "Fase de Decaimiento"); }
                else { dto.put("color", "green"); dto.put("estado", "Sin riesgo"); }
            } else {
                dto.put("tratamiento", "Sin tratamiento");
                dto.put("progreso", 0);
                dto.put("color", "gray");
                dto.put("estado", "PENDIENTE");
            }
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lista-gestion")
    public ResponseEntity<List<Map<String, Object>>> obtenerPacientesGestion() {
        List<Patient> pacientes = patientRepository.findAll();

        return ResponseEntity.ok(pacientes.stream().map(p -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("nombre", p.getUser() != null ? p.getUser().getNombreCompleto() : "Desconocido");
            dto.put("cip", p.getDni());
            dto.put("valorEmocional", p.getValorEmocional());

            // Usamos watchId para determinar el estado en la lista
            dto.put("watchEstado", p.getWatchId() != null ? "Activo" : "No vinculado");

            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);
            if (t != null && t.getRadioisotopo() != null) {
                double dosiInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosiInicial, t.getFechaInicio());

                // ESTA ES LA LÍNEA QUE MANTIENE TUS MBQ VISIBLES
                dto.put("tratamiento", t.getRadioisotopo() + " (" + String.format("%.2f", activitatActual) + " MBq)");

                double progress = (dosiInicial > 0) ? (activitatActual / dosiInicial) * 100 : 0;
                dto.put("progreso", (int) Math.round(progress));
                if (activitatActual > 400) { dto.put("color", "red"); dto.put("estado", "Fase Inicial"); }
                else if (activitatActual > 1) { dto.put("color", "yellow"); dto.put("estado", "Fase de Decaimiento"); }
                else { dto.put("color", "green"); dto.put("estado", "Sin riesgo"); }
            } else {
                dto.put("tratamiento", "Sin tratamiento");
                dto.put("progreso", 0);
                dto.put("color", "gray");
                dto.put("estado", "PENDIENTE");
            }
            return dto;
        }).collect(Collectors.toList()));
    }

    private double calcularActivitatActual(String isotopo, double dosiInicial, LocalDateTime fechaInicio) {
        if (isotopo == null) return dosiInicial;
        double tMedHores = (isotopo.contains("I-131") || isotopo.contains("Iodo")) ? 192.48 :
                (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) ? 159.36 :
                        (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) ? 46164.0 : -1;

        if (tMedHores == -1) return dosiInicial;
        long horesTranscorregudes = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();
        return dosiInicial * Math.pow(0.5, (double) horesTranscorregudes / tMedHores);
    }

    // --- NUEVO ENDPOINT PARA RECIBIR LA BATERÍA DEL RELOJ ---
    @PostMapping("/{cip}/actualizar-telemetria")
    @Transactional
    public ResponseEntity<?> actualizarTelemetriaDesdeReloj(@PathVariable String cip, @RequestBody WatchEstadoDTO datosReloj) {
        return patientRepository.findByDni(cip).map(p -> {
            p.setWatchBattery(datosReloj.getPorcentajeBateria());
            p.setWatchUltimaSinc(LocalDateTime.now());
            patientRepository.save(p);

            deviceRepository.findByPatientDni(cip).ifPresent(device -> {
                device.setEstado("Activo");
                device.setUltimaConexion(LocalDateTime.now());
                deviceRepository.save(device);
            });

            return ResponseEntity.ok(Map.of("message", "Telemetría actualizada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cip}/update-watch")
    @Transactional
    public ResponseEntity<?> actualizarSmartwatch(@PathVariable String cip, @RequestBody Map<String, Object> payload) {
        return deviceRepository.findByPatientDni(cip).map(device -> {
            if (payload.containsKey("estado") && payload.get("estado").toString().contains("Batería Baja")) {
                Notification nota = new Notification();
                nota.setMensaje("¡Atención! Smartwatch con batería baja.");
                nota.setFechaEnvio(LocalDateTime.now());
                nota.setLeida(false);
                nota.setPatient(device.getPatient());
                nota.setDoctor(device.getPatient().getDoctorAsignado());
                notificationRepository.save(nota);
            }
            if (payload.containsKey("estado")) device.setEstado((String) payload.get("estado"));
            device.setUltimaConexion(LocalDateTime.now());
            deviceRepository.save(device);
            return ResponseEntity.ok(Map.of("message", "Dispositivo actualizado."));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "No hay dispositivo.")));
    }

    @PostMapping("/{cip}/send-instruction")
    @Transactional
    public ResponseEntity<?> enviarInstruccionAlReloj(@PathVariable String cip, @RequestBody Map<String, String> payload) {
        return patientRepository.findByDni(cip).map(p -> {
            String mensajeTexto = payload.get("mensaje");
            if (mensajeTexto == null || mensajeTexto.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Mensaje vacío"));
            Notification nota = new Notification();
            nota.setMensaje("CONSEJO MÉDICO: " + mensajeTexto);
            nota.setFechaEnvio(LocalDateTime.now());
            nota.setLeida(false);
            nota.setPatient(p);
            nota.setDoctor(p.getDoctorAsignado());
            notificationRepository.save(nota);
            return ResponseEntity.ok(Map.of("message", "Instrucción enviada al Smartwatch."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{cip}/informe-alta")
    public void descargarPdf(@PathVariable String cip, HttpServletResponse response) throws IOException {
        Patient p = patientRepository.findByDni(cip).orElseThrow();
        Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);
        double act = calcularActivitatActual(t.getRadioisotopo(), t.getDosis(), t.getFechaInicio());
        String estado = (act > 400) ? "Fase Inicial" : (act > 1) ? "Fase de Decaimiento" : "Sin riesgo / EXEMPT";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Informe_Alta_" + cip + ".pdf");
        pdfService.exportarInformeAlta(response, p, t, act, estado);
    }

    @GetMapping("/count-total")
    public ResponseEntity<Map<String, Long>> obtenerTotalPacientes() {
        return ResponseEntity.ok(Map.of("count", patientRepository.count()));
    }

    // --- CORREGIDO PARA EVITAR EL ERROR 500 ---
    @PostMapping("/{cip}/register-view")
    @Transactional
    public ResponseEntity<?> registrarVisita(@PathVariable String cip, HttpServletRequest request) {
        try {
            String email = (String) request.getAttribute("userEmail");
            if (email == null) return ResponseEntity.ok("No auth");

            User docUser = userRepository.findByEmail(email).orElse(null);
            Patient p = patientRepository.findByDni(cip).orElse(null);

            if (docUser != null && docUser.getDoctor() != null && p != null) {
                UserActivity actividad = activityRepository
                        .findByDoctorIdAndPatientId(docUser.getDoctor().getId(), p.getId())
                        .orElse(new UserActivity());

                actividad.setDoctor(docUser.getDoctor());
                actividad.setPatient(p);
                actividad.setDescripcion("Última revisión del perfil");
                actividad.setFechaAccion(LocalDateTime.now());
                activityRepository.save(actividad);
            }
            return ResponseEntity.ok().build(); // Retorna OK siempre para no romper React
        } catch (Exception e) {
            return ResponseEntity.ok("Error controlado");
        }
    }

    @GetMapping("/recent-patients")
    public ResponseEntity<?> obtenerPacientesRecientes(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        User docUser = userRepository.findByEmail(email).orElse(null);
        if (docUser == null || docUser.getDoctor() == null) return ResponseEntity.ok(List.of());
        try {
            List<UserActivity> lista = activityRepository.findRecentByDoctor(docUser.getDoctor().getId());
            return ResponseEntity.ok(lista.stream().limit(4).map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("nombre", a.getPatient().getUser().getNombreCompleto());
                map.put("cip", a.getPatient().getDni());
                map.put("fecha", a.getFechaAccion());
                map.put("descripcion", a.getDescripcion());
                return map;
            }).collect(Collectors.toList()));
        } catch (Exception e) { return ResponseEntity.ok(List.of()); }
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<?> obtenerDashboardPaciente(@PathVariable Long userId) {
        return patientRepository.findByUserId(userId).map(p -> {
            Map<String, Object> dashboard = new HashMap<>();
            String nombreCompleto = p.getUser().getNombreCompleto();
            dashboard.put("nombre", nombreCompleto.split(" ")[0]);
            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);
            if (t != null) {
                double dosisInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosisInicial, t.getFechaInicio());
                double porcentajeRestante = (activitatActual / dosisInicial) * 100;
                int progreso = (int) Math.round(100 - porcentajeRestante);
                dashboard.put("progreso", Math.max(0, Math.min(100, progreso)));
                dashboard.put("radioisotopo", t.getRadioisotopo());
            } else { dashboard.put("progreso", 0); }
            dashboard.put("proximaCita", "Pendiente de confirmar");
            return ResponseEntity.ok(dashboard);
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Paciente no encontrado")));
    }

    @PostMapping("/{userId}/update-mood")
    @Transactional
    public ResponseEntity<?> actualizarEstadoEmocional(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        return patientRepository.findByUserId(userId).map(p -> {
            String moodString = payload.get("mood");
            int moodValue = switch (moodString) {
                case "happy" -> 1;
                case "neutral" -> 2;
                case "straight" -> 3;
                case "sad" -> 4;
                default -> 2;
            };
            p.setValorEmocional(moodValue);
            patientRepository.save(p);
            return ResponseEntity.ok(Map.of("message", "Estado emocional actualizado"));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Paciente no encontrado")));
    }

    @PostMapping("/{userId}/contact-doctor")
    @Transactional
    public ResponseEntity<?> contactarDoctor(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        return patientRepository.findByUserId(userId).map(p -> {
            try {
                String asunto = payload.get("asunto");
                String mensaje = payload.get("mensaje");
                Notification nota = new Notification();
                nota.setMensaje("CONSULTA DE SOPORTE: " + mensaje);
                nota.setAsunto(asunto);
                nota.setFechaEnvio(LocalDateTime.now());
                nota.setLeida(false);
                nota.setPatient(p);
                nota.setDoctor(p.getDoctorAsignado());
                notificationRepository.save(nota);
                String emailDoctor = p.getDoctorAsignado().getUser().getEmail();
                emailService.enviarCorreoSoporte(emailDoctor, payload.get("asunto"), payload.get("mensaje"), payload.get("nombre"), payload.get("correo"));
                return ResponseEntity.ok(Map.of("message", "Mensaje enviado con éxito."));
            } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage())); }
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Paciente no encontrado")));
    }
}