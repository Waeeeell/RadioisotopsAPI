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

    // Inyectamos tu repositorio existente
    public PatientResourceProvider(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class; // Indicamos que este es un proveedor de Pacientes FHIR
    }

    // Este método permite buscar en: /fhir/Patient?identifier=12345678Z
    @Search
    public List<Patient> searchByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) TokenParam identifier) {
        // Buscamos en tu base de datos por DNI
        radioisotops.api.com.example.demo.model.Patient localPatient =
                patientRepository.findByDni(identifier.getValue()).orElse(null);

        if (localPatient == null) {
            return Collections.emptyList();
        }

        // Convertimos tu entidad al recurso FHIR y lo devolvemos en una lista
        return Collections.singletonList(mapToFhir(localPatient));
    }

    // --- MAPEO: De tu BD al estándar HL7 FHIR ---
    private Patient mapToFhir(radioisotops.api.com.example.demo.model.Patient localPatient) {
        Patient fhirPatient = new Patient();

        // ID lógico
        fhirPatient.setId(localPatient.getId().toString());

        // Identificador (DNI)
        if (localPatient.getDni() != null) {
            fhirPatient.addIdentifier()
                    .setSystem("urn:oid:2.16.724.4.8.10.1") // OID para DNI español
                    .setValue(localPatient.getDni());
        }

        // Identificador (Seguridad Social)
        if (localPatient.getNumSs() != null) {
            fhirPatient.addIdentifier()
                    .setSystem("urn:oid:2.16.724.4.8.10.2") // OID para SS española
                    .setValue(localPatient.getNumSs());
        }

        // Fecha de nacimiento (Conversión de LocalDate a Date)
        if (localPatient.getFechaNacimiento() != null) {
            Date date = Date.from(localPatient.getFechaNacimiento()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            fhirPatient.setBirthDate(date);
        }

        return fhirPatient;
    }
}