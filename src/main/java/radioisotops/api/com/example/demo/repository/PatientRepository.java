/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de PatientRepository]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import radioisotops.api.com.example.demo.model.Patient;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByDni(String dni);
    Optional<Patient> findByWatchId(String watchId);
}