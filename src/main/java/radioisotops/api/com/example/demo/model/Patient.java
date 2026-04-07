package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients") // O "pacientes"
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK del paciente

    @Column(name = "fecha_nacimiento", nullable = true) // Cambiado a true
    private LocalDate fechaNacimiento;// Mejor LocalDate para fechas sin hora

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(name = "num_ss", nullable = false, unique = true)
    private String numSs;

    @Column(name = "valor_emocional")
    private Integer valorEmocional = 50;

    // RELACIÓN: CON USER (FOREIGN KEY) ---
    // Un paciente es un usuario
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    // RELACIÓN: CON DOCTOR (FOREIGN KEY) ---
    // Muchos pacientes pueden estar asignados a un mismo Doctor
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id") // Esta es tu "Id_Medico_Asign" del draw.io
    private Doctor doctorAsignado;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private Device device;

    // --- Constructores ---
    public Patient() {
    }

    public Patient(Long id, LocalDate fechaNacimiento, String dni, String numSs, User user, Doctor doctorAsignado) {
        this.id = id;
        this.fechaNacimiento = fechaNacimiento;
        this.dni = dni;
        this.numSs = numSs;
        this.user = user;
        this.doctorAsignado = doctorAsignado;
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumSs() {
        return numSs;
    }

    public void setNumSs(String numSs) {
        this.numSs = numSs;
    }

    public Integer getValorEmocional() { return valorEmocional; }

    public void setValorEmocional(Integer valorEmocional) { this.valorEmocional = valorEmocional; }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Doctor getDoctorAsignado() {
        return doctorAsignado;
    }

    public void setDoctorAsignado(Doctor doctorAsignado) {
        this.doctorAsignado = doctorAsignado;
    }

    public Device getDevice() { return device; }

    public void setDevice(Device device) { this.device = device; }
}