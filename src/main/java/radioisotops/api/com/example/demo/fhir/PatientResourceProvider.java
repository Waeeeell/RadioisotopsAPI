/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de PatientResourceProvider]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.fhir;

import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;
import radioisotops.api.com.example.demo.repository.PatientRepository;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class PatientResourceProvider implements IResourceProvider {

    private final PatientRepository patientRepository;

    public PatientResourceProvider(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class; // Indicamos que este es un proveedor de Pacientes FHIR
    }

    @Search
    public List<Patient> searchByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam identifier) {
        radioisotops.api.com.example.demo.model.Patient localPatient =
                patientRepository.findByDni(identifier.getValue()).orElse(null);

        if (localPatient == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(mapToFhir(localPatient));
    }

    private Patient mapToFhir(radioisotops.api.com.example.demo.model.Patient localPatient) {
        Patient fhirPatient = new Patient();

        fhirPatient.setId(localPatient.getId().toString());

        if (localPatient.getDni() != null) {
            fhirPatient.addIdentifier()
                    .setSystem("urn:oid:2.16.724.4.8.10.1")
                    .setValue(localPatient.getDni());
        }

        if (localPatient.getNumSs() != null) {
            fhirPatient.addIdentifier()
                    .setSystem("urn:oid:2.16.724.4.8.10.2")
                    .setValue(localPatient.getNumSs());
        }

        if (localPatient.getFechaNacimiento() != null) {
            Date date = Date.from(localPatient.getFechaNacimiento()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            fhirPatient.setBirthDate(date);
        }

        return fhirPatient;
    }
}