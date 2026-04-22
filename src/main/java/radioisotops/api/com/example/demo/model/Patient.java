package radioisotops.api.com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_nacimiento", nullable = true)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(name = "num_ss", nullable = false, unique = true)
    private String numSs;

    @Column(name = "valor_emocional")
    private Integer valorEmocional = 50;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctorAsignado;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private Device device;

    // Campos para la integración con el Smartwatch
    private String watchId;
    private String watchModel;
    private Integer watchBattery;
    private LocalDateTime watchUltimaSinc;

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

    public Integer getValorEmocional() {
        return valorEmocional;
    }

    public void setValorEmocional(Integer valorEmocional) {
        this.valorEmocional = valorEmocional;
    }

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

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getWatchId() {
        return watchId;
    }

    public void setWatchId(String watchId) {
        this.watchId = watchId;
    }

    public String getWatchModel() {
        return watchModel;
    }

    public void setWatchModel(String watchModel) {
        this.watchModel = watchModel;
    }

    public Integer getWatchBattery() {
        return watchBattery;
    }

    public void setWatchBattery(Integer watchBattery) {
        this.watchBattery = watchBattery;
    }

    public LocalDateTime getWatchUltimaSinc() {
        return watchUltimaSinc;
    }

    public void setWatchUltimaSinc(LocalDateTime watchUltimaSinc) {
        this.watchUltimaSinc = watchUltimaSinc;
    }
}