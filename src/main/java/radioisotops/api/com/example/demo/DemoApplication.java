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
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.repository.DoctorRepository;
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
    CommandLineRunner initDatabase(UserRepository userRepository, DoctorRepository doctorRepository) {
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
            
            System.out.println("Verificación de usuarios iniciales completada.");
        };
    }
}