/*
================================================================================
PROJECT:       [RADIOISOTOPO]
VERSION:       1.0.0
DESCRIPTION:   [Parte de WebConfig]
AUTHOR:        [Marcos, Wael]
UPDATED:       [06/05/2026]
================================================================================
*/
package radioisotops.api.com.example.demo.security;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Cross-Origin-Resource-Policy", "Authorization");
    }

    @Bean
    public FilterRegistrationBean<Filter> resourcePolicyFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>((request, response, chain) -> {
            HttpServletResponse res = (HttpServletResponse) response;

            res.setHeader("Cross-Origin-Resource-Policy", "cross-origin");

            res.setHeader("X-Content-Type-Options", "nosniff");

            chain.doFilter(request, response);
        });

        bean.setOrder(0);
        return bean;
    }
}