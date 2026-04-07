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

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PdfGeneratorService pdfService;

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

            // Asignamos el médico al paciente para las notificaciones
            patient.setDoctorAsignado(doc);

            Patient patientGuardado = patientRepository.save(patient);

            Treatment treatment = new Treatment();
            treatment.setRadioisotopo((String) datosTratamiento.get("radioisotopo"));

            Object dosisObj = datosTratamiento.get("dosis");
            String unidad = (String) datosTratamiento.get("unidad");

            if (dosisObj != null && !dosisObj.toString().isEmpty()) {
                double valorDosis = Double.parseDouble(dosisObj.toString());
                double dosisEnMBq;

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
            treatment.setInstrucciones("Monitorización activa basada en decaimiento físico real.");

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
            dto.put("nombre", p.getUser() != null ? p.getUser().getNombreCompleto() : "Desconocido");
            dto.put("cip", p.getDni());
            dto.put("valorEmocional", p.getValorEmocional());

            Device dev = p.getDevice();
            if (dev != null) {
                dto.put("watchEstado", dev.getEstado());
                dto.put("watchUltimaSinc", dev.getUltimaConexion());
                dto.put("watchSerie", dev.getSerieNum());
            } else {
                dto.put("watchEstado", "No vinculado");
            }

            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

            if (t != null && t.getRadioisotopo() != null) {
                double dosiInicial = t.getDosis();
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), dosiInicial, t.getFechaInicio());

                String isoOriginal = t.getRadioisotopo();
                String isoBonito = isoOriginal;
                if (isoOriginal.contains("I-131")) isoBonito = "Iodo-131";
                else if (isoOriginal.contains("Lu-177")) isoBonito = "Lutecio-177";
                else if (isoOriginal.contains("Co-60")) isoBonito = "Cobalto-60";

                dto.put("tratamiento", isoBonito + " (" + String.format("%.2f", activitatActual) + " MBq)");

                double progress = (activitatActual / dosiInicial) * 100;
                dto.put("progreso", (int) Math.round(progress));

                if (activitatActual > 400) {
                    dto.put("color", "red");
                    dto.put("estado", "Fase Inicial");
                } else if (activitatActual > 1) {
                    dto.put("color", "yellow");
                    dto.put("estado", "Fase de Decaimiento");

                    // Lógica de notificación automática de decaimiento
                    if (activitatActual <= 400 && activitatActual > 395) {
                        Notification avisoAlta = new Notification();
                        avisoAlta.setMensaje("El paciente " + p.getUser().getNombreCompleto() + " ha entrado en Fase de Decaimiento. Ya es seguro para el alta ambulatoria.");
                        avisoAlta.setFechaEnvio(LocalDateTime.now());
                        avisoAlta.setLeida(false);
                        avisoAlta.setPatient(p);
                        avisoAlta.setDoctor(p.getDoctorAsignado());
                        notificationRepository.save(avisoAlta);
                    }
                } else {
                    dto.put("color", "green");
                    dto.put("estado", "Sin riesgo");
                }
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
        double tMedHores;
        if (isotopo.contains("I-131") || isotopo.contains("Iodo")) {
            tMedHores = 192.48;
        } else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) {
            tMedHores = 159.36;
        } else if (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) {
            tMedHores = 46164.0;
        } else {
            return dosiInicial;
        }

        long horesTranscorregudes = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();
        return dosiInicial * Math.pow(0.5, (double) horesTranscorregudes / tMedHores);
    }

    @PostMapping("/{cip}/update-mood")
    public ResponseEntity<?> actualizarEstadoEmocional(@PathVariable String cip, @RequestBody Map<String, Integer> payload) {
        return patientRepository.findByDni(cip).map(p -> {
            Integer nuevoValor = payload.get("valor");
            if (nuevoValor == null || nuevoValor < 0 || nuevoValor > 100) return ResponseEntity.badRequest().body("Valor no válido");
            p.setValorEmocional(nuevoValor);
            patientRepository.save(p);
            return ResponseEntity.ok("Estado emocional actualizado.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cip}/update-watch")
    @Transactional
    public ResponseEntity<?> actualizarSmartwatch(@PathVariable String cip, @RequestBody Map<String, Object> payload) {
        return deviceRepository.findByPatientDni(cip).map(device -> {
            // Notificación por batería baja
            if (payload.containsKey("estado") && payload.get("estado").toString().contains("Batería Baja")) {
                Notification nota = new Notification();
                nota.setMensaje("¡Atención! El Smartwatch de " + device.getPatient().getUser().getNombreCompleto() + " tiene batería baja.");
                nota.setFechaEnvio(LocalDateTime.now());
                nota.setLeida(false);
                nota.setPatient(device.getPatient());
                nota.setDoctor(device.getPatient().getDoctorAsignado());
                notificationRepository.save(nota);
            }

            if (payload.containsKey("estado")) {
                device.setEstado((String) payload.get("estado"));
            }
            device.setUltimaConexion(LocalDateTime.now());
            deviceRepository.save(device);
            return ResponseEntity.ok("Dispositivo " + device.getSerieNum() + " actualizado.");
        }).orElse(ResponseEntity.status(404).body("No hay dispositivo para este paciente."));
    }

    @PostMapping("/{cip}/send-instruction")
    @Transactional
    public ResponseEntity<?> enviarInstruccionAlReloj(@PathVariable String cip, @RequestBody Map<String, String> payload) {
        return patientRepository.findByDni(cip).map(p -> {
            String mensajeTexto = payload.get("mensaje");

            if (mensajeTexto == null || mensajeTexto.isEmpty()) {
                return ResponseEntity.badRequest().body("El mensaje no puede estar vacío");
            }

            Notification notaParaReloj = new Notification();
            notaParaReloj.setMensaje("CONSEJO MÉDICO: " + mensajeTexto);
            notaParaReloj.setFechaEnvio(LocalDateTime.now());
            notaParaReloj.setLeida(false);
            notaParaReloj.setPatient(p);
            notaParaReloj.setDoctor(p.getDoctorAsignado());

            notificationRepository.save(notaParaReloj);

            return ResponseEntity.ok("Mensaje enviado a la cola del Smartwatch de " + p.getUser().getNombreCompleto());
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{cip}/informe-alta")
    public void descargarPdf(@PathVariable String cip, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Informe_Alta_" + cip + ".pdf";
        response.setHeader(headerKey, headerValue);

        Patient p = patientRepository.findByDni(cip).orElseThrow();
        Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

        double act = calcularActivitatActual(t.getRadioisotopo(), t.getDosis(), t.getFechaInicio());
        String estado = (act > 400) ? "Fase Inicial" : (act > 1) ? "Fase de Decaimiento" : "Sin riesgo / EXEMPT";

        pdfService.exportarInformeAlta(response, p, t, act, estado);
    }
}