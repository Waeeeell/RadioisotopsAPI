package radioisotops.api.com.example.demo.service;

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

    // Se ejecuta cada hora (3600000 ms)
    @Scheduled(fixedRate = 3600000)
    public void verificarDecaimientoYAlertas() {
        List<Patient> pacientes = patientRepository.findAll();

        for (Patient p : pacientes) {
            Treatment t = treatmentRepository.findFirstByPatientOrderByFechaInicioDesc(p);

            if (t != null && t.getRadioisotopo() != null) {
                double activitatActual = calcularActivitatActual(t.getRadioisotopo(), t.getDosis(), t.getFechaInicio());

                // Alerta automática: Si baja de 400 MBq y no tiene notificaciones de alta aún
                if (activitatActual <= 400 && activitatActual > 390) {
                    generarNotificacion(p, "El paciente " + p.getUser().getNombreCompleto() +
                            " ha entrado en Fase de Decaimiento. Ya es seguro para el alta.");
                }
            }
        }
        System.out.println("Vigilancia horaria completada: " + LocalDateTime.now());
    }

    private void generarNotificacion(Patient p, String mensaje) {
        Notification nota = new Notification();
        nota.setMensaje(mensaje);
        nota.setFechaEnvio(LocalDateTime.now());
        nota.setLeida(false);
        nota.setPatient(p);
        nota.setDoctor(p.getDoctorAsignado());
        notificationRepository.save(nota);
    }

    // Usamos la misma lógica del Excel que pusimos en el Controller
    private double calcularActivitatActual(String isotopo, double dosiInicial, LocalDateTime fechaInicio) {
        double tMedHores;
        if (isotopo.contains("I-131")) tMedHores = 192.48;
        else if (isotopo.contains("Lu-177")) tMedHores = 159.36;
        else if (isotopo.contains("Co-60")) tMedHores = 46164.0;
        else return dosiInicial;

        long hores = java.time.Duration.between(fechaInicio, LocalDateTime.now()).toHours();
        return dosiInicial * Math.pow(0.5, (double) hores / tMedHores);
    }
}