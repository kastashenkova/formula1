package org.example.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@TestConfiguration(proxyBeanMethods = false)
public class NotificationStubConfiguration {

    @Bean(destroyMethod = "stop")
    public WireMockServer notificationServer() {
        WireMockServer server = new WireMockServer(options().dynamicPort());
        server.start();

        server.stubFor(post(urlPathMatching("/.*/messages")).willReturn(ok()));

        return server;
    }

    @Bean
    public DynamicPropertyRegistrar notificationProperties(WireMockServer notificationServer) {
        return registry -> {
            registry.add("formula1.notification.whatsapp.api-url", notificationServer::baseUrl);
        };
    }
}
