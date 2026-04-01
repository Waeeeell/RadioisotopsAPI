package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import radioisotops.api.com.example.demo.model.Patient;
import radioisotops.api.com.example.demo.model.Treatment;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    Treatment findFirstByPatientOrderByFechaInicioDesc(Patient p);
}
