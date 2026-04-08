package radioisotops.api.com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    private String estado;

    @Column(name = "hospital_ref")
    private String hospitalRef;

    @Column(columnDefinition = "VARCHAR(50) DEFAULT 'Castellano'")
    private String idioma = "Castellano";

    @Column(name = "zona_horaria", columnDefinition = "VARCHAR(100) DEFAULT 'Europa/Madrid (CET)'")
    private String zonaHoraria = "Europa/Madrid (CET)";

    @Column(name = "notif_bateria")
    private boolean notifBateria = true;

    @Column(name = "notif_desconexion")
    private boolean notifDesconexion = true;

    @Column(name = "notif_resumen")
    private boolean notifResumen = false;

    @Column(name = "notif_radiacion")
    private boolean notifRadiacion = true;

    @Column(name = "notif_vitales")
    private boolean notifVitales = true;

    @Column(name = "notif_sincro")
    private boolean notifSincro = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Doctor doctor;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Patient patient;

    public User() {
    }

    public User(Long id, String nombreCompleto, String email, String password, String rol,
                LocalDateTime fechaRegistro, String estado, String hospitalRef) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
        this.hospitalRef = hospitalRef;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getHospitalRef() { return hospitalRef; }
    public void setHospitalRef(String hospitalRef) { this.hospitalRef = hospitalRef; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    // Getters y Setters de Preferencias
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }

    public boolean isNotifBateria() { return notifBateria; }
    public void setNotifBateria(boolean notifBateria) { this.notifBateria = notifBateria; }

    public boolean isNotifDesconexion() { return notifDesconexion; }
    public void setNotifDesconexion(boolean notifDesconexion) { this.notifDesconexion = notifDesconexion; }

    public boolean isNotifResumen() { return notifResumen; }
    public void setNotifResumen(boolean notifResumen) { this.notifResumen = notifResumen; }

    public boolean isNotifRadiacion() { return notifRadiacion; }
    public void setNotifRadiacion(boolean notifRadiacion) { this.notifRadiacion = notifRadiacion; }

    public boolean isNotifVitales() { return notifVitales; }
    public void setNotifVitales(boolean notifVitales) { this.notifVitales = notifVitales; }

    public boolean isNotifSincro() { return notifSincro; }
    public void setNotifSincro(boolean notifSincro) { this.notifSincro = notifSincro; }
}