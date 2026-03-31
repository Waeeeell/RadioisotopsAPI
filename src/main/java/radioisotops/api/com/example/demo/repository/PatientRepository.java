package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import radioisotops.api.com.example.demo.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
