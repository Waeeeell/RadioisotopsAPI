package radioisotops.api.com.example.demo.dto;

public record PreferenciasDTO(
        String idioma,
        String zonaHoraria,
        boolean bateriaBaja,
        boolean desconexionBiometrica,
        boolean resumenSemanal,
        boolean radiacionSegura,
        boolean anomaliaVitales,
        boolean falloSincronizacion
) {}