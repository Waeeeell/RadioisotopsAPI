/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de Notification Controller]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
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

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    private static final Map<String, String> MENSAJES_OFICIALES = new HashMap<>() {{
        put("FASE_ALTA", "Dosi administrada-400MBq: Dormir sol, rentar roba separada, dues descàrregues de cisterna, distància 1m amb adults, no contacte amb infants i embarassades, beu molta aigua.");
        put("FASE_DECAIMIENTO", "400MBq-2MBq: La radioactivitat està disminuint, mantingui les precaucions bàsiques: Dormir sol, rentar roba separada, dues descàrregues de cisterna, distància 1m amb adults, no contacte amb infants i embarassades, beu molta aigua.");
        put("FASE_EXENCION", "1MBq-0MBq: Exempció, normalitza les relacions socials.");
    }};

    @GetMapping("/me")
    public ResponseEntity<?> obtenerMisNotificaciones(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) return ResponseEntity.status(401).body("No autorizado");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getDoctor() == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(notificationRepository.findByDoctorIdOrderByFechaEnvioDesc(user.getDoctor().getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setLeida(true);
            notificationRepository.save(n);
            return ResponseEntity.ok(Map.of("message", "Notificación leída"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarNoLeidas(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) return ResponseEntity.ok(Map.of("unreadCount", 0));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getDoctor() == null) return ResponseEntity.ok(Map.of("unreadCount", 0));
        return ResponseEntity.ok(Map.of("unreadCount", notificationRepository.countByDoctorIdAndLeidaFalse(user.getDoctor().getId())));
    }

    @GetMapping("/patient/{cip}")
    public ResponseEntity<?> obtenerMensajesParaPaciente(@PathVariable String cip) {
        List<Notification> instrucciones = notificationRepository.findByPatientDniAndLeidaFalse(cip);
        if (!instrucciones.isEmpty()) {
            instrucciones.forEach(n -> n.setLeida(true));
            notificationRepository.saveAll(instrucciones);
        }
        return ResponseEntity.ok(instrucciones);
    }

    @GetMapping("/consultas")
    public ResponseEntity<?> obtenerConsultasRecibidas(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getDoctor() == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(notificationRepository.findByDoctorIdAndAsuntoIsNotNullOrderByFechaEnvioDesc(user.getDoctor().getId()));
    }

    @PostMapping("/patient/{cip}/send-instruction")
    @Transactional
    public ResponseEntity<?> enviarInstruccionReloj(@PathVariable String cip, @RequestBody Map<String, String> payload) {
        return patientRepository.findByDni(cip).map(p -> {
            String claveMensaje = payload.get("clave"); // El front envía "FASE_ALTA"
            String textoFinal = MENSAJES_OFICIALES.getOrDefault(claveMensaje, payload.get("mensaje"));

            if (textoFinal == null || textoFinal.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mensaje no definido"));
            }

            Notification nota = new Notification();
            nota.setMensaje(textoFinal);
            nota.setAsunto("Consonancia de Salud");
            nota.setFechaEnvio(LocalDateTime.now());
            nota.setLeida(false);
            nota.setPatient(p);
            nota.setDoctor(p.getDoctorAsignado());
            notificationRepository.save(nota);

            return ResponseEntity.ok(Map.of("message", "Instrucció enviada"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count-today")
    public ResponseEntity<?> obtenerConteoAlertasHoy() {
        try {
            long conteo = notificationRepository.countByFechaEnvioAfter(LocalDate.now().atStartOfDay());
            return ResponseEntity.ok(Map.of("todayCount", conteo));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("todayCount", 0));
        }
    }
}