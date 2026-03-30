package radioisotops.api.com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import radioisotops.api.com.example.demo.model.Doctor;
import radioisotops.api.com.example.demo.model.User;
import radioisotops.api.com.example.demo.repository.UserRepository;
import radioisotops.api.com.example.demo.repository.DoctorRepository;
import java.time.LocalDateTime;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// Crear el usuario administrador automáticamente al arrancar
	// Añade el DoctorRepository como parámetro
	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository,
			radioisotops.api.com.example.demo.repository.DoctorRepository doctorRepository) {
		return args -> {
			if (userRepository.count() == 0) {
				// 1. Creamos el Usuario Base
				User medico = new User();
				medico.setNombreCompleto("Kuronami Phantom");
				medico.setEmail("kurophan@hospital.com");
				medico.setContraseña("1234!");
				medico.setRol("MEDICO"); // Importante: Le ponemos rol MEDICO
				medico.setEstado("ACTIVO");
				medico.setHospitalRef("Hospital Central");
				medico.setFechaRegistro(LocalDateTime.now());

				// Guardamos el usuario para que se genere su ID
				userRepository.save(medico);

				// 2. Creamos los datos extra del Doctor y los enlazamos
				Doctor datosDoctor = new Doctor();
				datosDoctor.setEspecialidad("Oncología Radioterápica");
				datosDoctor.setColegiadoNum("COL-123456");
				datosDoctor.setUser(medico); // Enlazamos el Doctor con el Usuario

				// Guardamos el doctor
				doctorRepository.save(datosDoctor);

				System.out.println("=========================================");
				System.out.println("✅ MÉDICO CREADO (USER + DOCTOR)");
				System.out.println("👉 Email: user@hospital.com");
				System.out.println("👉 Clave: 1234");
				System.out.println("=========================================");
			}
		};
	}
}