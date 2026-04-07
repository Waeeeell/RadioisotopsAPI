package radioisotops.api.com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "doctors") // O "medicos", como prefieras que se llame la tabla en la BD
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK del médico

    @Column(nullable = false)
    private String especialidad;

    @Column(name = "colegiado_num", nullable = false, unique = true)
    private String colegiadoNum;

    // --- RELACIÓN CON USER (FOREIGN KEY) ---
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    // --- Constructores ---
    public Doctor() {
    }

    public Doctor(Long id, String especialidad, String colegiadoNum, User user) {
        this.id = id;
        this.especialidad = especialidad;
        this.colegiadoNum = colegiadoNum;
        this.user = user;
    }

    // --- Getters y Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getColegiadoNum() {
        return colegiadoNum;
    }

    public void setColegiadoNum(String colegiadoNum) {
        this.colegiadoNum = colegiadoNum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}