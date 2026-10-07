package formula1.notification;

import formula1.notification.service.EmailSender;
import formula1.notification.service.WhatsAppSender;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(NotifierProperties.class)
@ConditionalOnProperty(prefix = "formula1.notification", name = "enabled", havingValue = "true", matchIfMissing = false)
public class NotifierAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(NotifierAutoConfiguration.class);

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnMissingBean
    public RestTemplate notificationRestTemplate() {
        log.info("Registered bean RestTemplate from autoconfiguration NotifierAutoConfiguration");

        return new RestTemplate();
    }

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnProperty(prefix = "formula1.notification.whatsapp", name = "token")
    public WhatsAppSender whatsAppSender(
            RestTemplate notificationRestTemplate,
            NotifierProperties properties) {
        log.info("Registered bean WhatsAppSender from autoconfiguration NotifierAutoConfiguration");

        return new WhatsAppSender(
                notificationRestTemplate,
                properties.getWhatsapp().getToken(),
                properties.getWhatsapp().getPhoneNumberId(),
                properties.getWhatsapp().getApiUrl()
        );
    }

    @Bean
    @ConditionalOnClass(JavaMailSender.class)
    @ConditionalOnProperty(prefix = "formula1.notification.email", name = "host")
    public EmailSender emailSender(NotifierProperties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(properties.getEmail().getHost());
        mailSender.setPort(properties.getEmail().getPort());
        mailSender.setUsername(properties.getEmail().getUsername());
        mailSender.setPassword(properties.getEmail().getPassword());

        Properties mailProperties = mailSender.getJavaMailProperties();
        mailProperties.put("mail.smtp.auth", String.valueOf(properties.getEmail().isAuth()));
        mailProperties.put("mail.smtp.starttls.enable", String.valueOf(properties.getEmail().isStarttlsEnable()));

        return new EmailSender(mailSender);
    }
}