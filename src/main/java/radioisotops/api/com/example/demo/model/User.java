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

    @Column(name = "profile_pic_url")
    private String profilePicUrl;

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
    private Boolean notifBateria = true;

    @Column(name = "notif_desconexion")
    private Boolean notifDesconexion = true;

    @Column(name = "notif_resumen")
    private Boolean notifResumen = false;

    @Column(name = "notif_radiacion")
    private Boolean notifRadiacion = true;

    @Column(name = "notif_vitales")
    private Boolean notifVitales = true;

    @Column(name = "notif_sincro")
    private Boolean notifSincro = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Doctor doctor;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Patient patient;

    public User() {
    }

    // --- GETTERS Y SETTERS ---

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
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

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public Boolean getNotifBateria() {
        return notifBateria != null ? notifBateria : true;
    }

    public void setNotifBateria(Boolean notifBateria) {
        this.notifBateria = notifBateria;
    }

    public Boolean getNotifDesconexion() {
        return notifDesconexion != null ? notifDesconexion : true;
    }

    public void setNotifDesconexion(Boolean notifDesconexion) {
        this.notifDesconexion = notifDesconexion;
    }

    public Boolean getNotifResumen() {
        return notifResumen != null ? notifResumen : false;
    }

    public void setNotifResumen(Boolean notifResumen) {
        this.notifResumen = notifResumen;
    }

    public Boolean getNotifRadiacion() {
        return notifRadiacion != null ? notifRadiacion : true;
    }

    public void setNotifRadiacion(Boolean notifRadiacion) {
        this.notifRadiacion = notifRadiacion;
    }

    public Boolean getNotifVitales() {
        return notifVitales != null ? notifVitales : true;
    }

    public void setNotifVitales(Boolean notifVitales) {
        this.notifVitales = notifVitales;
    }

    public Boolean getNotifSincro() {
        return notifSincro != null ? notifSincro : false;
    }

    public void setNotifSincro(Boolean notifSincro) {
        this.notifSincro = notifSincro;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}