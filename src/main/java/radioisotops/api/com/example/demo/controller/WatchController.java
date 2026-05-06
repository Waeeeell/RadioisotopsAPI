/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de Watch Controller]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.WatchEstadoDTO;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/watch")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS })
public class WatchController {

    @Autowired private PatientRepository patientRepository;
    @Autowired private TreatmentRepository treatmentRepository;
    @Autowired private DeviceRepository deviceRepository;
    // Ya no es estrictamente necesario el NotificationRepository aquí para la UI del paciente
    @Autowired private NotificationRepository notificationRepository;

    @GetMapping("/estado/{cip}")
    public ResponseEntity<?> getEstadoReloj(@PathVariable String cip) {

        Patient patient = patientRepository.findByDni(cip).orElse(null);
        if (patient == null) return ResponseEntity.status(404).body("Paciente no encontrado");

        Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(patient);
        if (t == null) return ResponseEntity.status(404).body("Sin tratamiento activo");

        int diasTotales     = calcularDiasTotalesAislamiento(t.getRadioisotopo());
        double dosisInicial = t.getDosis();
        double actividadActual = calcularActividadActual(t.getRadioisotopo(), dosisInicial, t.getFechaInicio());

        // Progreso
        double pct = (dosisInicial > 0)
                ? Math.max(0.0, Math.min(1.0, 1.0 - (actividadActual / dosisInicial)))
                : 0.0;

        int diasSuperados = Math.max(0, Math.min(diasTotales, (int) Math.floor(pct * diasTotales)));
        int diasRestantes = diasTotales - diasSuperados;
        int diaActual     = Math.min(diasSuperados + 1, diasTotales);

        // Batería
        int bateria = (patient.getWatchBattery() != null && patient.getWatchBattery() > 0)
                ? patient.getWatchBattery() : 72;

        // ── MENSAJES ESTRICTAMENTE CLÍNICOS ───────────────────────────────────

        // HomeScreen: Frase general de la etapa.
        String mensajeHome = generarMensajeHome(actividadActual);

        // ActivityScreen: Instrucciones específicas y rigurosas que rotan.
        List<String> instrucciones = generarInstruccionesFase(actividadActual);

        // *ELIMINADO*: El bloque que inyectaba notificaciones del médico se ha borrado
        // para evitar que se crucen alertas del panel web en el reloj del paciente.

        // ── DTO ───────────────────────────────────────────────────────────────
        WatchEstadoDTO dto = new WatchEstadoDTO();
        dto.setDiasSuperados(diasSuperados);
        dto.setDiasRestantes(diasRestantes);
        dto.setDiaActual(diaActual);
        dto.setPorcentajeBateria(bateria);
        dto.setMensajeApi(mensajeHome);
        dto.setInstrucciones(instrucciones);

        // Legacy (por si se usa en otra vista)
        dto.setTitulo(generarTitulo(diasSuperados, diasTotales));
        dto.setMensajeParte1(generarMensajeParte1(actividadActual));
        dto.setMensajeResaltado(generarMensajeResaltado(actividadActual));
        dto.setMensajeParte2(generarMensajeParte2(actividadActual));

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/actualizar-telemetria/{cip}")
    public ResponseEntity<?> actualizarTelemetria(
            @PathVariable String cip,
            @RequestBody WatchEstadoDTO datosReloj) {

        return patientRepository.findByDni(cip).map(p -> {
            Device device = deviceRepository.findByPatientDni(cip).orElse(new Device());
            device.setPatient(p);
            device.setEstado("Activo");
            p.setWatchBattery(datosReloj.getPorcentajeBateria());
            p.setWatchUltimaSinc(LocalDateTime.now());
            patientRepository.save(p);
            deviceRepository.save(device);
            return ResponseEntity.ok("Telemetría actualizada");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * HomeScreen: Frase general de la etapa. NUNCA se mostrarán los consejos detallados aquí.
     */
    private String generarMensajeHome(double actividad) {
        if (actividad > 400) return "Aislamiento total.\nEvita todo contacto.";
        if (actividad > 1)   return "La radiactividad baja.\nMantén precauciones.";
        return "Exención completada.\n¡Vida normal!";
    }

    /**
     * ActivityScreen: Instrucciones específicas y rigurosas.
     */
    private List<String> generarInstruccionesFase(double actividad) {
        List<String> lista = new ArrayList<>();

        if (actividad > 400) {
            // Fase inicial (Dosi administrada - 400 MBq)
            lista.add("Dormir sol");
            lista.add("Rentar roba separada");
            lista.add("Dos descàrregues de cisterna");
            lista.add("Distància 1m amb adults");
            lista.add("No contacte amb infants i embarassades");
            lista.add("Beu molta aigua");

        } else if (actividad > 1) { // Límite inferior 1 MBq
            // Fase de decaimiento (400 MBq - 1 MBq)
            lista.add("La radioactivitat està disminuint");
            lista.add("Mantingues precaucions bàsiques:");
            lista.add("Dormir sol");
            lista.add("Rentar roba separada");
            lista.add("Dos descàrregues de cisterna");
            lista.add("Distància 1m amb adults");
            lista.add("No contacte amb infants i embarassades");
            lista.add("Beu molta aigua");

        } else {
            // Fase final — exención (1 MBq - 0 MBq)
            lista.add("Exempció");
            lista.add("Normalitza les relacions socials");
        }

        return lista;
    }

    private int calcularDiasTotalesAislamiento(String r) {
        if (r == null)                                          return 8;
        if (r.contains("I-131")  || r.contains("Iodo"))        return 8;
        if (r.contains("Lu-177") || r.contains("Lutecio"))     return 7;
        if (r.contains("Co-60")  || r.contains("Cobalto"))     return 14;
        return 8;
    }

    private double calcularActividadActual(String isotopo, double dosisInicial, LocalDateTime fechaInicio) {
        double tMedHoras;
        if      (isotopo == null)                                            tMedHoras = -1;
        else if (isotopo.contains("I-131")  || isotopo.contains("Iodo"))    tMedHoras = 192.48;
        else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) tMedHoras = 159.36;
        else if (isotopo.contains("Co-60")  || isotopo.contains("Cobalto")) tMedHoras = 46164.0;
        else                                                                 tMedHoras = -1;

        if (tMedHoras == -1) return dosisInicial;
        long horas = ChronoUnit.HOURS.between(fechaInicio, LocalDateTime.now());
        return dosisInicial * Math.pow(0.5, (double) horas / tMedHoras);
    }

    private String generarTitulo(int diasSuperados, int diasTotales) {
        if (diasSuperados == 0)               return "Inicio del tratamiento";
        if (diasSuperados >= diasTotales / 2) return "¡Ya vas por la mitad!";
        return "Vas por el día " + (diasSuperados + 1);
    }

    private String generarMensajeParte1(double actividad) {
        if (actividad > 400) return "Debes permanecer en ";
        if (actividad > 1)   return "Puedes moverte por ";
        return "Puedes salir a ";
    }

    private String generarMensajeResaltado(double actividad) {
        if (actividad > 400) return "aislamiento total";
        if (actividad > 1)   return "casa con precaución";
        return "dar un paseo";
    }

    private String generarMensajeParte2(double actividad) {
        if (actividad > 400) return ".\nSin visitas ni salidas.";
        if (actividad > 1)   return ".\nEvita salir al exterior.";
        return ",\n¡solo 15 minutos!";
    }
}