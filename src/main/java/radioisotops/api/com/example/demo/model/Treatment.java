package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "treatments")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String radioisotopo; // Ej: Yodo-131, Tecnecio-99m

    private Double dosis; // Cantidad administrada

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    private String instrucciones;

    // --- RELACIÓN CON PATIENT ---
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    // --- RELACIÓN CON DOCTOR (El que lo receta) ---
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    private Doctor doctor;

    // --- Constructores ---
    public Treatment() {
    }

    public Treatment(Long id, String radioisotopo, Double dosis, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String instrucciones, Patient patient, Doctor doctor) {
        this.id = id;
        this.radioisotopo = radioisotopo;
        this.dosis = dosis;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.instrucciones = instrucciones;
        this.patient = patient;
        this.doctor = doctor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRadioisotopo() {
        return radioisotopo;
    }

    public void setRadioisotopo(String radioisotopo) {
        this.radioisotopo = radioisotopo;
    }

    public Double getDosis() {
        return dosis;
    }

    public void setDosis(Double dosis) {
        this.dosis = dosis;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
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