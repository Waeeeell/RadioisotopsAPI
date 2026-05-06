/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de NotificationRepository]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
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
    long countByFechaEnvioAfter(LocalDateTime fecha);

    List<Notification> findByPatientDniAndLeidaFalse(String dni);
    List<Notification> findByDoctorIdAndAsuntoIsNotNullOrderByFechaEnvioDesc(Long doctorId);
}