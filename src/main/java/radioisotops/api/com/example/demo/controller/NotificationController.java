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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> obtenerMisNotificaciones(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || user.getDoctor() == null) {
            return ResponseEntity.badRequest().body("Médico no encontrado");
        }

        List<Notification> notas = notificationRepository.findByDoctorIdOrderByFechaEnvioDesc(user.getDoctor().getId());
        return ResponseEntity.ok(notas);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setLeida(true);
            notificationRepository.save(n);
            return ResponseEntity.ok("Notificación leída");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarNoLeidas(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getDoctor() == null) return ResponseEntity.ok(0);

        long count = notificationRepository.countByDoctorIdAndLeidaFalse(user.getDoctor().getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // Obtener mensajes/instrucciones destinados a un paciente específico
    @GetMapping("/patient/{cip}")
    public ResponseEntity<?> obtenerMensajesParaPaciente(@PathVariable String cip) {
        List<Notification> instrucciones = notificationRepository.findByPatientDniAndLeidaFalse(cip);

        instrucciones.forEach(n -> n.setLeida(true));
        notificationRepository.saveAll(instrucciones);

        return ResponseEntity.ok(instrucciones);
    }

    @GetMapping("/count-today")
    public ResponseEntity<?> contarAlertasHoy(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");
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