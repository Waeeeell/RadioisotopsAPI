package radioisotops.api.com.example.demo.dto;

public class PatientTableDTO {

    private String nombre;
    private String cip;
    private String radioisotopo;
    private double dosis;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCip() {
        return cip;
    }

    public void setCip(String cip) {
        this.cip = cip;
    }

    public String getRadioisotopo() {
        return radioisotopo;
    }

    public void setRadioisotopo(String radioisotopo) {
        this.radioisotopo = radioisotopo;
    }

    public double getDosis() {
        return dosis;
    }

    public void setDosis(double dosis) {
        this.dosis = dosis;
    }
}
