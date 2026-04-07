package radioisotops.api.com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000) // Aumentamos longitud por si el mensaje es largo
    private String mensaje;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    private Boolean leida = false; // Valor por defecto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    @JsonIgnore
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({"tratamientos", "doctorAsignado", "user", "device", "hibernateLazyInitializer", "handler"})
    private Patient patient;

    public Notification() {
    }

    public Notification(String mensaje, LocalDateTime fechaEnvio, Boolean leida, Patient patient, Doctor doctor) {
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.leida = leida;
        this.patient = patient;
        this.doctor = doctor;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getLeida() { return leida; }
    public void setLeida(Boolean leida) { this.leida = leida; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
}