package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {
    /* prova */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String serieNum; // Número de serie único del aparato

    private String tipo; // Ej: Contador Geiger, Sensor de pulso, etc.

    private String estado; // Activo, Inactivo, Batería Baja

    @Column(name = "ultima_conexion")
    private LocalDateTime ultimaConexion;

    // --- RELACIÓN CON PATIENT ---
    // Un dispositivo pertenece a un paciente específico
    @OneToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerieNum() {
        return serieNum;
    }

    public void setSerieNum(String serieNum) {
        this.serieNum = serieNum;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getUltimaConexion() {
        return ultimaConexion;
    }

    public void setUltimaConexion(LocalDateTime ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    // --- Constructor vacío (OBLIGATORIO para JPA) ---
    public Device() {
    }

    public Device(Long id, String serieNum, String tipo, String estado, LocalDateTime ultimaConexion, Patient patient) {
        this.id = id;
        this.serieNum = serieNum;
        this.tipo = tipo;
        this.estado = estado;
        this.ultimaConexion = ultimaConexion;
        this.patient = patient;
    }

    // --- Constructores ---

}