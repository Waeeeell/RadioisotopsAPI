package radioisotops.api.com.example.demo.fhir;

import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;
import radioisotops.api.com.example.demo.repository.PatientRepository;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class ObservationResourceProvider implements IResourceProvider {

    private final PatientRepository patientRepository;

    public ObservationResourceProvider(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Observation.class;
    }

    // Permite buscar telemetría por paciente: /fhir/Observation?subject=Patient/1
    @Search
    public List<Observation> getObservationsForPatient(@RequiredParam(name = Observation.SP_SUBJECT) ReferenceParam patientRef) {
        Long patientId = patientRef.getIdPartAsLong();
        radioisotops.api.com.example.demo.model.Patient localPatient =
                patientRepository.findById(patientId).orElse(null);

        if (localPatient == null || localPatient.getWatchBattery() == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(mapBatteryToObservation(localPatient));
    }

    private Observation mapBatteryToObservation(radioisotops.api.com.example.demo.model.Patient localPatient) {
        Observation observation = new Observation();
        observation.setId("batt-" + localPatient.getId());
        observation.setStatus(Observation.ObservationStatus.FINAL);

        // Código LOINC internacional para nivel de batería en dispositivos médicos
        observation.setCode(new CodeableConcept().addCoding(new Coding()
                .setSystem("http://loinc.org")
                .setCode("8775-6")
                .setDisplay("Battery level of medical device")));

        // Referencia al paciente (vínculo FHIR)
        observation.setSubject(new Reference("Patient/" + localPatient.getId()));

        // Valor de la batería (en porcentaje)
        Quantity value = new Quantity();
        value.setValue(localPatient.getWatchBattery())
                .setUnit("%")
                .setSystem("http://unitsofmeasure.org")
                .setCode("%");
        observation.setValue(value);

        // Fecha de la medición
        if (localPatient.getWatchUltimaSinc() != null) {
            Date date = Date.from(localPatient.getWatchUltimaSinc().atZone(ZoneId.systemDefault()).toInstant());
            observation.setEffective(new DateTimeType(date));
        }

        return observation;
    }
}