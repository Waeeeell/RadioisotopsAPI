package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    private Boolean leida; // Para saber si el médico ya la vio (true/false)

    // --- RELACIÓN CON PATIENT (El que generó la alerta) ---
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // --- RELACIÓN CON DOCTOR (El que debe recibirla) ---
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // Constructor vacío
    public Notification() {
    }

    public Notification(Long id, String mensaje, LocalDateTime fechaEnvio, Boolean leida, Patient patient,
            Doctor doctor) {
        this.id = id;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.leida = leida;
        this.patient = patient;
        this.doctor = doctor;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}