/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de DemoApplication]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.springframework.scheduling.annotation.EnableAsync;
import radioisotops.api.com.example.demo.model.Doctor;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.model.Patient;
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
    CommandLineRunner initDatabase(UserRepository userRepository, DoctorRepository doctorRepository, PatientRepository patientRepository) {
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
            var pac = patientRepository.findByDni(cipBorrar);
            if (pac.isPresent()) {
                User u = pac.get().getUser();
                if (u != null) {
                    u.setEstado("BORRAR");
                    userRepository.save(u);
                    System.out.println("OK - Paciente " + cipBorrar + " (" + u.getNombreCompleto() + ") marcado como BORRAR.");
                } else {
                    System.out.println("ERROR - Paciente " + cipBorrar + " encontrado pero sin User asociado.");
                }
            } else {
                System.out.println("ERROR - Paciente con CIP " + cipBorrar + " NO encontrado en la BD.");
                var porEmail = userRepository.findByEmail(cipBorrar.toLowerCase() + "@catsalut.cat");
                if (porEmail.isPresent()) {
                    porEmail.get().setEstado("BORRAR");
                    userRepository.save(porEmail.get());
                    System.out.println("OK - Encontrado por email y marcado como BORRAR.");
                } else {
                    System.out.println("ERROR - Tampoco encontrado por email " + cipBorrar.toLowerCase() + "@catsalut.cat");
                }
            }

            System.out.println("Verificación de usuarios iniciales completada.");
        };
    }
}