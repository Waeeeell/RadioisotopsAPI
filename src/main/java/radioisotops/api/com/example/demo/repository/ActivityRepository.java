package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import radioisotops.api.com.example.demo.model.UserActivity;
import radioisotops.api.com.example.demo.model.Doctor;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<UserActivity, Long> {
    Optional<UserActivity> findByDoctorIdAndPatientId(Long doctorId, Long patientId);

    @Query("SELECT a FROM UserActivity a WHERE a.doctor.id = :doctorId ORDER BY a.fechaAccion DESC")
    List<UserActivity> findRecentByDoctor(@Param("doctorId") Long doctorId);
}