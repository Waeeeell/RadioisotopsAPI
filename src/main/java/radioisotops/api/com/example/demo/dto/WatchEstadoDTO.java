/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de WatchEstadoDTO]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.dto;

import java.util.List;

public class WatchEstadoDTO {
    private int diasSuperados;
    private int diasRestantes;
    private int diaActual;

    private Integer porcentajeBateria;

    private String mensajeApi;
    private String titulo;
    private String mensajeParte1;
    private String mensajeResaltado;
    private String mensajeParte2;
    private List<String> instrucciones;

    public WatchEstadoDTO() {}

    public int getDiasSuperados() {
        return diasSuperados;
    }

    public void setDiasSuperados(int diasSuperados) {
        this.diasSuperados = diasSuperados;
    }

    public int getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(int diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public int getDiaActual() {
        return diaActual;
    }

    public void setDiaActual(int diaActual) {
        this.diaActual = diaActual;
    }

    public Integer getPorcentajeBateria() {
        return porcentajeBateria;
    }

    public void setPorcentajeBateria(Integer porcentajeBateria) {
        this.porcentajeBateria = porcentajeBateria;
    }

    public String getMensajeApi() {
        return mensajeApi;
    }

    public void setMensajeApi(String mensajeApi) {
        this.mensajeApi = mensajeApi;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensajeParte1() {
        return mensajeParte1;
    }

    public void setMensajeParte1(String mensajeParte1) {
        this.mensajeParte1 = mensajeParte1;
    }

    public String getMensajeResaltado() {
        return mensajeResaltado;
    }

    public void setMensajeResaltado(String mensajeResaltado) {
        this.mensajeResaltado = mensajeResaltado;
    }

    public String getMensajeParte2() {
        return mensajeParte2;
    }

    public void setMensajeParte2(String mensajeParte2) {
        this.mensajeParte2 = mensajeParte2;
    }

    public List<String> getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(List<String> instrucciones) {
        this.instrucciones = instrucciones;
    }
}