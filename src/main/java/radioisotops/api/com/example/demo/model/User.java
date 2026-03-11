package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios") // Así se llamará la tabla en tu base de datos real
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Tu Id (PK)

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contraseña; // En un futuro la encriptaremos

    @Column(nullable = false)
    private String rol; // Podría ser un Enum (MEDICO, PACIENTE, ADMIN)

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    private String estado;

    @Column(name = "hospital_ref")
    private String hospitalRef;

    // --- Constructor vacío (Obligatorio para Spring Boot) ---
    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getHospitalRef() {
        return hospitalRef;
    }

    public void setHospitalRef(String hospitalRef) {
        this.hospitalRef = hospitalRef;
    }

    public User(Long id, String nombreCompleto, String email, String contraseña, String rol,
            LocalDateTime fechaRegistro, String estado, String hospitalRef) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.contraseña = contraseña;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.hospitalRef = hospitalRef;
    }
}
