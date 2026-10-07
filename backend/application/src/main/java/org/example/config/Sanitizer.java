package org.example.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Pattern;

public class Sanitizer extends MessageConverter {

    private static final Pattern SENSITIVE_PATTERN =
            Pattern.compile("(?i)(password|auth|bearer token|secret|token|apiKey|creditCard|)\\s*[:=]\\s*\"?([^\"&\\s,]+)\"?");

    @Override
    public String convert(ILoggingEvent event) {
        var message = event.getFormattedMessage();
        if (message == null) {
            return "";
        }
        var sanitized = message.replace('\n', '_').replace('\r', '_');

        var matcher = SENSITIVE_PATTERN.matcher(sanitized);
        if (matcher.find()) {
            sanitized = matcher.replaceAll(mr -> mr.group(1) + "=\"********\"");
        }

        return sanitized;
    }
}
