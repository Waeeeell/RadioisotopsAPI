package radioisotops.api.com.example.demo.dto;

public class LoginResponseDTO {

    private Long id;
    private String email;
    private String nombreCompleto;
    private String rol;
    private String especialidad;
    private String colegiado;
    private String token;
    private boolean requiereCambioPassword; // El nuevo flag

    // Constructor actualizado con el nuevo parámetro
    public LoginResponseDTO(Long id, String email, String nombreCompleto, String rol,
                            String especialidad, String colegiado, String token,
                            boolean requiereCambioPassword) {
        this.id = id;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.especialidad = especialidad;
        this.colegiado = colegiado;
        this.token = token;
        this.requiereCambioPassword = requiereCambioPassword;
    }

    public LoginResponseDTO() {}

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getColegiado() { return colegiado; }
    public void setColegiado(String colegiado) { this.colegiado = colegiado; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isRequiereCambioPassword() {
        return requiereCambioPassword;
    }

    public void setRequiereCambioPassword(boolean requiereCambioPassword) {
        this.requiereCambioPassword = requiereCambioPassword;
    }
}