/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de PreferenciasDTO]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
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