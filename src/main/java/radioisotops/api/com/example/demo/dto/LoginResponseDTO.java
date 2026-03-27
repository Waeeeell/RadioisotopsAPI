package radioisotops.api.com.example.demo.dto;

public class LoginResponseDTO {

    private String email;
    private String nombreCompleto;
    private String rol;
    private String especialidad;
    private String colegiado;

    public LoginResponseDTO(String email, String nombreCompleto, String rol, String especialidad, String colegiado) {
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.especialidad = especialidad;
        this.colegiado = colegiado;
    }

    // Getters y Setters para que Spring pueda serializar el objeto a JSON

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
}