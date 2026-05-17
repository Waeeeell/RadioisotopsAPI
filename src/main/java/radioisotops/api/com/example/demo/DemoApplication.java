/*
===============================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de DemoApplication]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
===============================================================================
*/
package radioisotops.api.com.example.demo;

import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import radioisotops.api.com.example.demo.model.Doctor;
import radioisotops.api.com.example.demo.model.Patient;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.repository.DoctorRepository;
import radioisotops.api.com.example.demo.repository.PatientRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.LocalDateTime;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, DoctorRepository doctorRepository,
                                   PatientRepository patientRepository, EntityManager entityManager,
                                   PlatformTransactionManager transactionManager) {
        return args -> {
            if (userRepository.findByEmail("admin@hospital.com").isEmpty()) {
                User admin = new User();
                admin.setNombreCompleto("Admin General");
                admin.setEmail("admin@hospital.com");
                admin.setPassword("admin123&!");
                admin.setRol("ADMIN");
                admin.setEstado("ACTIVO");
                admin.setHospitalRef("Hospital Central");
                admin.setFechaRegistro(LocalDateTime.now());
                userRepository.save(admin);

                System.out.println("Administrador creado con éxito.");
            }

            String cipBorrar = "LOMA0208110059";
            var pacienteOpt = patientRepository.findByDni(cipBorrar);
            if (pacienteOpt.isPresent()) {
                var paciente = pacienteOpt.get();
                User user = paciente.getUser();
                if (user != null) {
                    TransactionTemplate tx = new TransactionTemplate(transactionManager);
                    tx.executeWithoutResult(status -> {
                        entityManager.createQuery("DELETE FROM Treatment t WHERE t.patient.id = :pid")
                            .setParameter("pid", paciente.getId())
                            .executeUpdate();
                        entityManager.createQuery("DELETE FROM Notification n WHERE n.patient.id = :pid")
                            .setParameter("pid", paciente.getId())
                            .executeUpdate();
                        entityManager.createQuery("DELETE FROM UserActivity a WHERE a.patient.id = :pid")
                            .setParameter("pid", paciente.getId())
                            .executeUpdate();
                        entityManager.createQuery("DELETE FROM Device d WHERE d.patient.id = :pid")
                            .setParameter("pid", paciente.getId())
                            .executeUpdate();
                        entityManager.flush();
                        entityManager.clear();
                        Patient p = entityManager.find(Patient.class, paciente.getId());
                        User u = entityManager.find(User.class, user.getId());
                        if (p != null) entityManager.remove(p);
                        if (u != null) entityManager.remove(u);
                    });
                    System.out.println("OK - Paciente " + cipBorrar + " (" + user.getNombreCompleto() + ") eliminado permanentemente.");
                } else {
                    System.out.println("ERROR - Paciente " + cipBorrar + " encontrado pero sin User asociado.");
                }
            } else {
                System.out.println("ERROR - Paciente con CIP " + cipBorrar + " NO encontrado en la BD.");
            }

            System.out.println("Verificación de usuarios iniciales completada.");
        };
    }
}
