package radioisotops.api.com.example.demo.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuración global de CORS para permitir peticiones desde el Frontend.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Exponemos la cabecera para que sea visible tras pasar el Proxy
                .exposedHeaders("Cross-Origin-Resource-Policy", "Authorization");
    }

    /**
     * FILTRO DE SEGURIDAD CRÍTICO:
     * Inyecta la cabecera 'Cross-Origin-Resource-Policy' en todas las respuestas.
     * Esto soluciona el error de "OpaqueResponseBlocking" y permite que las
     * imágenes se carguen correctamente a través del proxy de Cloudflare.
     */
    @Bean
    public FilterRegistrationBean<Filter> resourcePolicyFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>((request, response, chain) -> {
            HttpServletResponse res = (HttpServletResponse) response;

            // Permite que recursos (imágenes) sean leídos por otros dominios
            res.setHeader("Cross-Origin-Resource-Policy", "cross-origin");

            // También añadimos seguridad básica para evitar que el navegador "adivine" el tipo de contenido
            res.setHeader("X-Content-Type-Options", "nosniff");

            chain.doFilter(request, response);
        });

        // Establecemos el orden de ejecución al más alto para que se aplique siempre
        bean.setOrder(0);
        return bean;
    }
}