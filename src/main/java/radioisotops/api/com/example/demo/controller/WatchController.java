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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST,
        RequestMethod.OPTIONS })
public class WatchController {

    @Autowired private PatientRepository patientRepository;
    @Autowired private TreatmentRepository treatmentRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private NotificationRepository notificationRepository;

    @GetMapping("/estado/{cip}")
    public ResponseEntity<?> getEstadoReloj(@PathVariable String cip) {

        Patient patient = patientRepository.findByDni(cip).orElse(null);
        if (patient == null) return ResponseEntity.status(404).body("Paciente no encontrado");

        Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(patient);
        if (t == null) return ResponseEntity.status(404).body("Sin tratamiento activo");

        int diasTotales = calcularDiasTotalesAislamiento(t.getRadioisotopo());
        double dosisInicial = t.getDosis();
        double actividadActual = calcularActividadActual(t.getRadioisotopo(), dosisInicial, t.getFechaInicio());

        // Progreso (mismo criterio que la web)
        double porcentajeSuperado = (dosisInicial > 0)
                ? Math.max(0.0, Math.min(1.0, 1.0 - (actividadActual / dosisInicial)))
                : 0.0;

        int diasSuperados = (int) Math.floor(porcentajeSuperado * diasTotales);
        diasSuperados = Math.max(0, Math.min(diasTotales, diasSuperados));
        int diasRestantes = diasTotales - diasSuperados;
        int diaActual = Math.min(diasSuperados + 1, diasTotales);

        // Batería Dinámica: Priorizamos el valor guardado en el paciente
        int bateria = (patient.getWatchBattery() != null) ? patient.getWatchBattery() : 0;

        // Lógica de respaldo si el valor en Patient es 0 o null
        if (bateria == 0) {
            Device device = deviceRepository.findByPatientDni(cip).orElse(null);
            if (device != null && device.getEstado() != null) {
                if (device.getEstado().contains("Batería Baja")) bateria = 15;
                else if (device.getEstado().equals("Activo"))    bateria = 85;
                else bateria = 72; // Valor por defecto original
            } else {
                bateria = 72; // Valor por defecto original
            }
        }

        // ── MENSAJES ──────────────────────────────────────────────────────────

        // Instrucciones base según fase (para ActivityScreen, rotación)
        List<String> instrucciones = generarInstruccionesFase(actividadActual);

        // Mensaje corto para HomeScreen (1 línea)
        String mensajeHome = generarMensajeHome(actividadActual);

        // Si el médico envió un mensaje directo → va primero en la rotación
        List<Notification> notifsMedico = notificationRepository.findByPatientDniAndLeidaFalse(cip);
        if (!notifsMedico.isEmpty()) {
            String rawMedico = notifsMedico.get(0).getMensaje()
                    .replace("CONSEJO MÉDICO: ", "").trim();

            String[] partes = rawMedico.split("[,.]");
            List<String> instruccionesMedico = new ArrayList<>();
            for (String p : partes) {
                String limpio = p.trim();
                if (!limpio.isEmpty()) instruccionesMedico.add(limpio);
            }
            instruccionesMedico.addAll(instrucciones);
            instrucciones = instruccionesMedico;
            mensajeHome = instruccionesMedico.get(0);
        }

        // ── DTO ───────────────────────────────────────────────────────────────
        WatchEstadoDTO dto = new WatchEstadoDTO();
        dto.setDiasRestantes(diasSuperados);
        dto.setDiasSuperados(diasRestantes);
        dto.setDiaActual(diaActual);
        dto.setPorcentajeBateria(bateria);
        dto.setMensajeApi(mensajeHome);
        dto.setInstrucciones(instrucciones);

        dto.setTitulo(generarTitulo(diasSuperados, diasTotales));
        dto.setMensajeParte1(generarMensajeParte1(actividadActual));
        dto.setMensajeResaltado(generarMensajeResaltado(actividadActual));
        dto.setMensajeParte2(generarMensajeParte2(actividadActual));

        return ResponseEntity.ok(dto);
    }

    // ── RECEPTOR DE DATOS DEL RELOJ ──────────────────────────────────────────

    @PostMapping("/actualizar-telemetria/{cip}")
    public ResponseEntity<?> actualizarTelemetria(@PathVariable String cip, @RequestBody WatchEstadoDTO datosReloj) {
        return patientRepository.findByDni(cip).map(p -> {
            Device device = deviceRepository.findByPatientDni(cip).orElse(new Device());

            device.setPatient(p);
            device.setEstado("Activo");

            // Guardamos el porcentaje real enviado por el reloj
            p.setWatchBattery(datosReloj.getPorcentajeBateria());
            p.setWatchUltimaSinc(LocalDateTime.now());

            patientRepository.save(p);
            deviceRepository.save(device);

            return ResponseEntity.ok("Telemetría actualizada");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generarMensajeHome(double actividad) {
        if (actividad > 400) return "Mantente en aislamiento total.";
        if (actividad > 100) return "Quédate en casa, evita salir.";
        if (actividad > 1)   return "Puedes salir 15 min al parque.";
        return "Aislamiento completado.";
    }

    private List<String> generarInstruccionesFase(double actividad) {
        List<String> lista = new ArrayList<>();
        if (actividad > 400) {
            lista.add("Duerme solo");
            lista.add("Ropa lavada por separado");
            lista.add("2 descargas cisterna");
            lista.add("1m de distancia con adultos");
            lista.add("Sin contacto con niños");
            lista.add("Sin contacto con embarazadas");
            lista.add("Bebe mucha agua");
        } else if (actividad > 1) {
            lista.add("Radioactividad disminuyendo");
            lista.add("Duerme solo");
            lista.add("Ropa lavada por separado");
            lista.add("1m de distancia con adultos");
            lista.add("Sin contacto con niños");
            lista.add("Sin contacto con embarazadas");
            lista.add("Bebe mucha agua");
        } else {
            lista.add("Aislamiento completado");
            lista.add("Normaliza tus relaciones");
            lista.add("Vida normal");
        }
        return lista;
    }

    private int calcularDiasTotalesAislamiento(String r) {
        if (r == null) return 8;
        if (r.contains("I-131")  || r.contains("Iodo")) return 8;
        if (r.contains("Lu-177") || r.contains("Lutecio")) return 7;
        if (r.contains("Co-60")  || r.contains("Cobalto")) return 14;
        return 8;
    }

    private double calcularActividadActual(String isotopo, double dosisInicial, LocalDateTime fechaInicio) {
        double tMedHoras;
        if (isotopo == null) { tMedHoras = -1; }
        else if (isotopo.contains("I-131") || isotopo.contains("Iodo")) { tMedHoras = 192.48; }
        else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) { tMedHoras = 159.36; }
        else if (isotopo.contains("Co-60")  || isotopo.contains("Cobalto")) { tMedHoras = 46164.0; }
        else { tMedHoras = -1; }

        if (tMedHoras == -1) return dosisInicial;
        long horas = ChronoUnit.HOURS.between(fechaInicio, LocalDateTime.now());
        return dosisInicial * Math.pow(0.5, (double) horas / tMedHoras);
    }

    private String generarTitulo(int diasSuperados, int diasTotales) {
        if (diasSuperados == 0) return "Inicio del tratamiento";
        if (diasSuperados >= diasTotales / 2) return "¡Ya vas por la mitad!";
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
        return ",\n¡solo 15 minutos!";
    }
}