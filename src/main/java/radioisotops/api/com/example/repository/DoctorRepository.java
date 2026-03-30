package radioisotops.api.com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import radioisotops.api.com.example.demo.model.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

}