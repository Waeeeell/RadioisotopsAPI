package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_configs")
public class AlertConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_parametro", nullable = false)
    private String tipoParametro; // Ej: "RADIACION"

    @Column(name = "umbral_min")
    private Double umbralMin;

    @Column(name = "umbral_max")
    private Double umbralMax;

    private String prioridad; // "ALTA", "MEDIA", "BAJA"

    // --- RELACIÓN CON PATIENT ---
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Constructor vacío
    public AlertConfig() {
    }

    public AlertConfig(Long id, String tipoParametro, Double umbralMin, Double umbralMax, String prioridad,
            Patient patient) {
        this.id = id;
        this.tipoParametro = tipoParametro;
        this.umbralMin = umbralMin;
        this.umbralMax = umbralMax;
        this.prioridad = prioridad;
        this.patient = patient;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoParametro() {
        return tipoParametro;
    }

    public void setTipoParametro(String tipoParametro) {
        this.tipoParametro = tipoParametro;
    }

    public Double getUmbralMin() {
        return umbralMin;
    }

    public void setUmbralMin(Double umbralMin) {
        this.umbralMin = umbralMin;
    }

    public Double getUmbralMax() {
        return umbralMax;
    }

    public void setUmbralMax(Double umbralMax) {
        this.umbralMax = umbralMax;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

}