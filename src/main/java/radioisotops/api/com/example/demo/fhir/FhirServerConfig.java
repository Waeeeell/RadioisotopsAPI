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
        // Inicializamos el servidor indicando que usaremos la versión R4 (el estándar actual)
        RestfulServer restfulServer = new RestfulServer(FhirContext.forR4());

        // --- AVISO: Estas dos líneas saldrán en ROJO temporalmente ---
        // Aquí conectaremos los traductores de tus datos en los siguientes pasos
        restfulServer.registerProvider(applicationContext.getBean(PatientResourceProvider.class));
        restfulServer.registerProvider(applicationContext.getBean(ObservationResourceProvider.class));

        // Le decimos a Spring Boot que este servidor escuchará en la ruta /fhir/
        return new ServletRegistrationBean<>(restfulServer, "/fhir/*");
    }
}