package radioisotops.api.com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radioisotops.api.com.example.demo.dto.WatchEstadoDTO;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/watch")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.OPTIONS })
public class WatchController {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private TreatmentRepository treatmentRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/estado/{cip}")
    public ResponseEntity<?> getEstadoReloj(@PathVariable String cip) {

        // 1. Buscar paciente por CIP/DNI
        Patient patient = patientRepository.findByDni(cip).orElse(null);
        if (patient == null) {
            return ResponseEntity.status(404).body("Paciente no encontrado");
        }

        // 2. Buscar tratamiento activo
        Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(patient);
        if (t == null) {
            return ResponseEntity.status(404).body("Sin tratamiento activo");
        }

        // 3. Total de días de aislamiento según radioisótopo
        int diasTotalesAislamiento = calcularDiasTotalesAislamiento(t.getRadioisotopo());

        // 4. Calcular actividad actual (mismo criterio que la web)
        double dosisInicial = t.getDosis();
        double actividadActual = calcularActividadActual(
                t.getRadioisotopo(), dosisInicial, t.getFechaInicio());

        // 5. ✅ CÁLCULO CORREGIDO — mismo criterio que PatientController (web)
        //    porcentajeSuperado: cuánto del tratamiento ha "decaído" (0.0 → 1.0)
        //    Cuando actividad = dosisInicial → 0% superado (día 0)
        //    Cuando actividad = 0            → 100% superado (día final)
        double porcentajeSuperado = (dosisInicial > 0)
                ? Math.max(0.0, Math.min(1.0, 1.0 - (actividadActual / dosisInicial)))
                : 0.0;

        // Entero estricto con floor: 2.9 días → 2, nunca 3
        int diasSuperados = (int) Math.floor(porcentajeSuperado * diasTotalesAislamiento);
        diasSuperados = Math.max(0, Math.min(diasTotalesAislamiento, diasSuperados));

        int diasRestantes = diasTotalesAislamiento - diasSuperados;
        int diaActual     = Math.min(diasSuperados + 1, diasTotalesAislamiento);

        // 6. Batería del dispositivo
        int bateria = 72;
        Device device = deviceRepository.findByPatientDni(cip).orElse(null);
        if (device != null && device.getEstado() != null) {
            if (device.getEstado().contains("Batería Baja"))
                bateria = 15;
            else if (device.getEstado().equals("Activo"))
                bateria = 85;
        }

        // 7. Mensaje: prioridad al médico, si no, dinámico según actividad
        String mensajeApi = generarMensajeDinamico(actividadActual, diasSuperados, diasTotalesAislamiento);
        List<Notification> instrucciones = notificationRepository.findByPatientDniAndLeidaFalse(cip);
        if (!instrucciones.isEmpty()) {
            mensajeApi = instrucciones.get(0).getMensaje().replace("CONSEJO MÉDICO: ", "");
        }

        // 8. Construir DTO
        WatchEstadoDTO dto = new WatchEstadoDTO();
        dto.setDiasSuperados(diasRestantes);
        dto.setDiasRestantes(diasSuperados);
        dto.setDiaActual(diaActual);
        dto.setPorcentajeBateria(bateria);
        dto.setMensajeApi(mensajeApi);

        // ActivityScreen — textos desglosados
        dto.setTitulo(generarTitulo(diasSuperados, diasTotalesAislamiento));
        dto.setMensajeParte1(generarMensajeParte1(actividadActual));
        dto.setMensajeResaltado(generarMensajeResaltado(actividadActual));
        dto.setMensajeParte2(generarMensajeParte2(actividadActual));

        return ResponseEntity.ok(dto);
    }

    // --- Helpers ---

    private int calcularDiasTotalesAislamiento(String radioisotopo) {
        if (radioisotopo == null)                                          return 8;
        if (radioisotopo.contains("I-131")  || radioisotopo.contains("Iodo"))     return 8;
        if (radioisotopo.contains("Lu-177") || radioisotopo.contains("Lutecio"))  return 7;
        if (radioisotopo.contains("Co-60")  || radioisotopo.contains("Cobalto"))  return 14;
        return 8;
    }

    private double calcularActividadActual(String isotopo, double dosisInicial, LocalDateTime fechaInicio) {
        double tMedHoras;

        if (isotopo == null) {
            tMedHoras = -1;
        } else if (isotopo.contains("I-131") || isotopo.contains("Iodo")) {
            tMedHoras = 192.48;
        } else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) {
            tMedHoras = 159.36;
        } else if (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) {
            tMedHoras = 46164.0;
        } else {
            tMedHoras = -1;
        }

        if (tMedHoras == -1) return dosisInicial;

        long horasTranscurridas = ChronoUnit.HOURS.between(fechaInicio, LocalDateTime.now());
        return dosisInicial * Math.pow(0.5, (double) horasTranscurridas / tMedHoras);
    }

    private String generarMensajeDinamico(double actividad, int diasSuperados, int diasTotales) {
        if (actividad > 400) return "Mantente en aislamiento.\nEvita contacto con otras personas.";
        if (actividad > 100) return "Puedes moverte por casa,\npero evita salir al exterior.";
        if (actividad > 1)   return "Puedes salir a dar un paseo\nde 15 minutos por el parque.";
        return "¡Aislamiento completado!\nYa puedes hacer vida normal.";
    }

    private String generarTitulo(int diasSuperados, int diasTotales) {
        if (diasSuperados == 0)                    return "Inicio del tratamiento";
        if (diasSuperados >= diasTotales / 2)      return "¡Ya vas por la mitad!";
        return "Vas por el día " + (diasSuperados + 1);
    }

    private String generarMensajeParte1(double actividad) {
        if (actividad > 400) return "Debes permanecer en ";
        if (actividad > 100) return "Puedes moverte por ";
        return "Puedes salir a ";
    }

    private String generarMensajeResaltado(double actividad) {
        if (actividad > 400) return "aislamiento total";
        if (actividad > 100) return "casa con precaución";
        return "dar un paseo";
    }

    private String generarMensajeParte2(double actividad) {
        if (actividad > 400) return ".\nSin visitas ni salidas.";
        if (actividad > 100) return ".\nEvita salir al exterior.";
        return ",\n¡pero recuerda, solo 15 minutos!";
    }
}