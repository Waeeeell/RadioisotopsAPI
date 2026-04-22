package radioisotops.api.com.example.demo.dto;

import java.util.List;

public class WatchEstadoDTO {
    private int diasSuperados;
    private int diasRestantes;
    private int diaActual;
    private int porcentajeBateria;
    private String mensajeApi;        // HomeScreen: 1 línea corta
    private String titulo;            // ActivityScreen: título
    private String mensajeParte1;
    private String mensajeResaltado;
    private String mensajeParte2;
    private List<String> instrucciones; // ActivityScreen: lista que rota

    public WatchEstadoDTO() {}

    public int getDiasSuperados() { return diasSuperados; }
    public void setDiasSuperados(int d) { this.diasSuperados = d; }

    public int getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(int d) { this.diasRestantes = d; }

    public int getDiaActual() { return diaActual; }
    public void setDiaActual(int d) { this.diaActual = d; }

    public int getPorcentajeBateria() { return porcentajeBateria; }
    public void setPorcentajeBateria(int p) { this.porcentajeBateria = p; }

    public String getMensajeApi() { return mensajeApi; }
    public void setMensajeApi(String m) { this.mensajeApi = m; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String t) { this.titulo = t; }

    public String getMensajeParte1() { return mensajeParte1; }
    public void setMensajeParte1(String m) { this.mensajeParte1 = m; }

    public String getMensajeResaltado() { return mensajeResaltado; }
    public void setMensajeResaltado(String m) { this.mensajeResaltado = m; }

    public String getMensajeParte2() { return mensajeParte2; }
    public void setMensajeParte2(String m) { this.mensajeParte2 = m; }

    public List<String> getInstrucciones() { return instrucciones; }
    public void setInstrucciones(List<String> i) { this.instrucciones = i; }
}