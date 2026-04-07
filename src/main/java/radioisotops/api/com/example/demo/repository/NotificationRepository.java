package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import radioisotops.api.com.example.demo.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByDoctorIdOrderByFechaEnvioDesc(Long doctorId);
    long countByDoctorIdAndLeidaFalse(Long doctorId);
    long countByDoctorIdAndFechaEnvioAfter(Long doctorId, LocalDateTime fecha);
    boolean existsByPatientIdAndLeidaFalse(Long patientId);

    List<Notification> findByPatientDniAndLeidaFalse(String dni);
}