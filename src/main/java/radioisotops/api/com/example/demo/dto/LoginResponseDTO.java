package radioisotops.api.com.example.demo.dto;

public class LoginResponseDTO {

    private Long id;
    private String email;
    private String nombreCompleto;
    private String rol;
    private String especialidad;
    private String colegiado;
    private String token;
    private boolean requiereCambioPassword;
    private String idioma;
    private String zonaHoraria;
    private boolean notifBateria;
    private boolean notifDesconexion;
    private boolean notifResumen;
    private boolean notifRadiacion;
    private boolean notifVitales;
    private boolean notifSincro;
    private String profilePicUrl; // ✅ NUEVO

    public LoginResponseDTO(Long id, String email, String nombreCompleto, String rol,
                            String especialidad, String colegiado, String token,
                            boolean requiereCambioPassword, String idioma, String zonaHoraria,
                            boolean notifBateria, boolean notifDesconexion, boolean notifResumen,
                            boolean notifRadiacion, boolean notifVitales, boolean notifSincro,
                            String profilePicUrl) { // ✅ NUEVO
        this.id = id;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.especialidad = especialidad;
        this.colegiado = colegiado;
        this.token = token;
        this.requiereCambioPassword = requiereCambioPassword;
        this.idioma = idioma;
        this.zonaHoraria = zonaHoraria;
        this.notifBateria = notifBateria;
        this.notifDesconexion = notifDesconexion;
        this.notifResumen = notifResumen;
        this.notifRadiacion = notifRadiacion;
        this.notifVitales = notifVitales;
        this.notifSincro = notifSincro;
        this.profilePicUrl = profilePicUrl; // ✅ NUEVO
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

    public boolean isRequiereCambioPassword() { return requiereCambioPassword; }
    public void setRequiereCambioPassword(boolean requiereCambioPassword) { this.requiereCambioPassword = requiereCambioPassword; }

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

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
}