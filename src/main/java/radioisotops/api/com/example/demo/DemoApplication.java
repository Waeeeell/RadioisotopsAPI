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
	CommandLineRunner initDatabase(UserRepository userRepository, DoctorRepository doctorRepository) {
		return args -> {
			if (userRepository.findByEmail("kurophan@hospital.com") == null) {
				User medico = new User();
				medico.setNombreCompleto("Kuronami Phantom");
				medico.setEmail("kurophan@hospital.com");
				medico.setContraseña("1234!");
				medico.setRol("MEDICO");
				medico.setEstado("ACTIVO");
				medico.setHospitalRef("Hospital Central");
				medico.setFechaRegistro(LocalDateTime.now());
				userRepository.save(medico);

				Doctor datosDoctor = new Doctor();
				datosDoctor.setEspecialidad("Oncología Radioterápica");
				datosDoctor.setColegiadoNum("COL-123457");
				datosDoctor.setUser(medico);
				doctorRepository.save(datosDoctor);
				
				System.out.println("Médico 'Kuronami' creado con éxito.");
			}

			if (userRepository.findByEmail("admin@hospital.com") == null) {
				User admin = new User();
				admin.setNombreCompleto("Admin General");
				admin.setEmail("admin@hospital.com");
				admin.setContraseña("admin123&!");
				admin.setRol("ADMIN");
				admin.setEstado("ACTIVO");
				admin.setHospitalRef("Hospital Central");
				admin.setFechaRegistro(LocalDateTime.now());
				userRepository.save(admin);
				
				System.out.println("Administrador creado con éxito.");
			}
			
			System.out.println("🚀 Verificación de usuarios iniciales completada.");
		};
	}
}