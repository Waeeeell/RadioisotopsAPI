/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de ObservationResourceProvider]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
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

        observation.setCode(new CodeableConcept().addCoding(new Coding()
                .setSystem("http://loinc.org")
                .setCode("8775-6")
                .setDisplay("Battery level of medical device")));

        observation.setSubject(new Reference("Patient/" + localPatient.getId()));

        Quantity value = new Quantity();
        value.setValue(localPatient.getWatchBattery())
                .setUnit("%")
                .setSystem("http://unitsofmeasure.org")
                .setCode("%");
        observation.setValue(value);

        if (localPatient.getWatchUltimaSinc() != null) {
            Date date = Date.from(localPatient.getWatchUltimaSinc().atZone(ZoneId.systemDefault()).toInstant());
            observation.setEffective(new DateTimeType(date));
        }

        return observation;
    }
}