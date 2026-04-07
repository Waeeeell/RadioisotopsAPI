package radioisotops.api.com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
// Configuración robusta para evitar bloqueos del Worker en peticiones frecuentes
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS}
)
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtener todas las notificaciones del médico logueado.
     */
    @GetMapping("/me")
    public ResponseEntity<?> obtenerMisNotificaciones(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) return ResponseEntity.status(401).body("No autorizado");

        User user = userRepository.findByEmail(email).orElse(null);

        // Si el usuario es ADMIN, no tendrá notificaciones de médico, devolvemos lista vacía
        if (user == null || user.getDoctor() == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Notification> notas = notificationRepository.findByDoctorIdOrderByFechaEnvioDesc(user.getDoctor().getId());
        return ResponseEntity.ok(notas);
    }

    /**
     * Marcar una notificación como leída.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setLeida(true);
            notificationRepository.save(n);
            return ResponseEntity.ok(Map.of("message", "Notificación leída"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Contador de notificaciones no leídas para la campana de la NavBar.
     */
    @GetMapping("/count")
    public ResponseEntity<?> contarNoLeidas(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) return ResponseEntity.ok(Map.of("unreadCount", 0));

        User user = userRepository.findByEmail(email).orElse(null);

        // Si no es médico o no existe, devolvemos 0 en formato JSON
        if (user == null || user.getDoctor() == null) {
            return ResponseEntity.ok(Map.of("unreadCount", 0));
        }

        long count = notificationRepository.countByDoctorIdAndLeidaFalse(user.getDoctor().getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Obtener mensajes/instrucciones destinados a un paciente específico por CIP/DNI.
     */
    @GetMapping("/patient/{cip}")
    public ResponseEntity<?> obtenerMensajesParaPaciente(@PathVariable String cip) {
        List<Notification> instrucciones = notificationRepository.findByPatientDniAndLeidaFalse(cip);

        if (!instrucciones.isEmpty()) {
            instrucciones.forEach(n -> n.setLeida(true));
            notificationRepository.saveAll(instrucciones);
        }

        return ResponseEntity.ok(instrucciones);
    }

    /**
     * Contador de alertas recibidas en el día actual para el Dashboard.
     */
    @GetMapping("/count-today")
    public ResponseEntity<?> contarAlertasHoy(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        if (email == null) return ResponseEntity.ok(Map.of("todayCount", 0));

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.getDoctor() == null) {
            return ResponseEntity.ok(Map.of("todayCount", 0));
        }

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();

        long count = notificationRepository.countByDoctorIdAndFechaEnvioAfter(
                user.getDoctor().getId(),
                inicioHoy
        );

        return ResponseEntity.ok(Map.of("todayCount", count));
    }
}