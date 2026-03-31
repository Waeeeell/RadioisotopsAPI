package radioisotops.api.com.example.demo.dto;

public class LoginResponseDTO {

    private Long id;
    private String email;
    private String nombreCompleto;
    private String rol;
    private String especialidad;
    private String colegiado;
    private String token;

    public LoginResponseDTO(Long id, String email, String nombreCompleto, String rol, String especialidad, String colegiado, String token) {
        this.id = id;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.especialidad = especialidad;
        this.colegiado = colegiado;
        this.token = token;
    }

    public LoginResponseDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
    }

    public String getToken() { // <--- Sin este Getter, el token no llegará a React
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}