/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de FhirServerConfig]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirServerConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServerRegistration() {
        RestfulServer restfulServer = new RestfulServer(FhirContext.forR4());

        restfulServer.registerProvider(applicationContext.getBean(PatientResourceProvider.class));
        restfulServer.registerProvider(applicationContext.getBean(ObservationResourceProvider.class));

        return new ServletRegistrationBean<>(restfulServer, "/fhir/*");
    }
}