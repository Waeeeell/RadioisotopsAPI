package radioisotops.api.com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import radioisotops.api.com.example.demo.model.*;
import radioisotops.api.com.example.demo.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTaskService {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private TreatmentRepository treatmentRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Tarea programada: Se ejecuta cada hora.
     * Calcula el decaimiento de todos los pacientes y genera alertas si el riesgo baja.
     */
    @Scheduled(fixedRate = 3600000)
    public void verificarDecaimientoYAlertas() {
        System.out.println("Iniciando ciclo de vigilancia nuclear: " + LocalDateTime.now());

        List<Patient> pacientes = patientRepository.findAll();

        for (Patient p : pacientes) {
            // Buscamos el tratamiento mas reciente del paciente
            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

            if (t != null && t.getRadioisotopo() != null) {
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), t.getDosis(), t.getFechaInicio());

                // UMbral de seguridad: 400 MBq (Limite estandar para permitir contacto social limitado)
                if (activitatActual <= 400 && activitatActual > 350) {
                    generarAlertaSeguridad(p, "ALERTA: El paciente " + p.getUser().getNombreCompleto() +
                            " ha bajado de los 400 MBq. Clasificado como Seguro para Alta / Fase de Decaimiento.");
                }

                // Alerta de seguridad extrema (Bateria o desconexion se gestionan en el Controller)
            }
        }
        System.out.println("Vigilancia completada con exito.");
    }

    private void generarAlertaSeguridad(Patient p, String mensaje) {
        // Evitamos duplicar alertas no leidas para no saturar al medico
        boolean yaExisteAlertaActiva = notificationRepository.existsByPatientIdAndLeidaFalse(p.getId());

        if (!yaExisteAlertaActiva) {
            Notification nota = new Notification();
            nota.setMensaje(mensaje);
            nota.setFechaEnvio(LocalDateTime.now());
            nota.setLeida(false);
            nota.setPatient(p);
            nota.setDoctor(p.getDoctorAsignado());

            notificationRepository.save(nota);
            System.out.println("Notificacion de seguridad enviada para: " + p.getUser().getNombreCompleto());
        }
    }

    /**
     * Logica de decaimiento fisico real A = A0 * 0.5^(t/T1/2)
     */
    private double calcularActivitatActual(String isotopo, double dosiInicial, LocalDateTime fechaInicio) {
        double tMedHores;
        if (isotopo.contains("I-131") || isotopo.contains("Iodo")) tMedHores = 192.48;
        else if (isotopo.contains("Lu-177") || isotopo.contains("Lutecio")) tMedHores = 159.36;
        else if (isotopo.contains("Co-60") || isotopo.contains("Cobalto")) tMedHores = 46164.0;
        else return dosiInicial;

        long hores = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();
        return dosiInicial * Math.pow(0.5, (double) hores / tMedHores);
    }
}