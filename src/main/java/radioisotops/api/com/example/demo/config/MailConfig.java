package radioisotops.api.com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return new org.springframework.mail.javamail.JavaMailSenderImpl();
    }
}
