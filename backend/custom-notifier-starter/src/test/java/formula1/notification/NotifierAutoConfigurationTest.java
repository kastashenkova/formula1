package formula1.notification;

import static org.assertj.core.api.Assertions.assertThat;

import formula1.notification.service.EmailSender;
import formula1.notification.service.WhatsAppSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class NotifierAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NotifierAutoConfiguration.class));

    @Test
    void shouldNotRegisterEmailSenderWhenPropertyDisabled() {
        runner.withPropertyValues("formula1.notification.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(EmailSender.class);
                });
    }

    @Test
    void shouldNotRegisterWhatsAppSenderWhenPropertyDisabled() {
        runner.withPropertyValues("formula1.notification.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(WhatsAppSender.class);
                });
    }

    @Test
    void shouldNotRegisterEmailSenderWhenPropertyMissing() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(EmailSender.class);
        });
    }

    @Test
    void shouldNotRegisterWhatsAppSenderWhenPropertyMissing() {
        runner.run(context -> {
            assertThat(context).doesNotHaveBean(WhatsAppSender.class);
        });
    }

    @Test
    void shouldRegisterEmailSenderWhenPropertyEnabled() {
        runner.withPropertyValues("formula1.notification.enabled=true",
                        "formula1.notification.email.host=smtp.gmail.com",
                        "formula1.notification.email.port=587",
                        "formula1.notification.email.username=test@gmail.com",
                        "formula1.notification.email.password=secret")
                .run(context -> {
                    assertThat(context).hasSingleBean(EmailSender.class);
                });
    }

    @Test
    void shouldRegisterWhatsAppSenderWhenPropertyEnabled() {
        runner.withPropertyValues("formula1.notification.enabled=true",
                        "formula1.notification.whatsapp.token=test-token",
                        "formula1.notification.whatsapp.phone-number-id=123456789")
                .run(context -> {
                    assertThat(context).hasSingleBean(WhatsAppSender.class);
                });
    }

    @Test
    void shouldAllowOverridingEmailSenderBean() {
        EmailSender customService = new EmailSender(null) {
        };

        runner.withPropertyValues("formula1.notification.enabled=true")
                .withBean("customEmailSender", EmailSender.class, () -> customService)
                .run(context -> {
                    assertThat(context).hasBean("customEmailSender");
                    assertThat(context.getBean(EmailSender.class)).isSameAs(customService);
                });
    }

    @Test
    void shouldAllowOverridingWhatsAppSenderBean() {
        WhatsAppSender customService = new WhatsAppSender(null,
                "token",
                "12345678",
                "https://example.com"); {
        };

        runner.withPropertyValues("formula1.notification.enabled=true")
                .withBean("customWhatsAppSender", WhatsAppSender.class, () -> customService)
                .run(context -> {
                    assertThat(context).hasSingleBean(WhatsAppSender.class);
                    assertThat(context.getBean(WhatsAppSender.class)).isSameAs(customService);
                });
    }
}
