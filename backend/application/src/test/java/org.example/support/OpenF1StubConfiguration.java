package org.example.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Stubs OpenF1 API:
 * Monaco 2024 - race with 2 drivers and 3 telemetry points,
 * Imola 2024 - race whose drivers request fails,
 * any other race - not found.
 */
@TestConfiguration(proxyBeanMethods = false)
public class OpenF1StubConfiguration {

    public static final int MONACO_SESSION_KEY = 9523;
    public static final int IMOLA_SESSION_KEY = 9515;

    @Bean(destroyMethod = "stop")
    public WireMockServer openF1Server() {
        WireMockServer server = new WireMockServer(options().dynamicPort());
        server.start();

        server.stubFor(get(urlPathEqualTo("/sessions")).atPriority(10)
                .willReturn(okJson("[]")));
        server.stubFor(get(urlPathEqualTo("/sessions")).atPriority(1)
                .withQueryParam("country_name", equalTo("Monaco"))
                .withQueryParam("year", equalTo("2024"))
                .withQueryParam("session_name", equalTo("Race"))
                .willReturn(okJson(session(MONACO_SESSION_KEY, "2024-05-26T13:00:00+00:00"))));
        server.stubFor(get(urlPathEqualTo("/sessions")).atPriority(1)
                .withQueryParam("country_name", equalTo("Imola"))
                .withQueryParam("year", equalTo("2024"))
                .willReturn(okJson(session(IMOLA_SESSION_KEY, "2024-05-19T13:00:00+00:00"))));

        server.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo(String.valueOf(MONACO_SESSION_KEY)))
                .willReturn(okJson("""
                        [
                          {"session_key": 9523, "driver_number": 1, "full_name": "Max VERSTAPPEN", "team_name": "Red Bull Racing"},
                          {"session_key": 9523, "driver_number": 16, "full_name": "Charles LECLERC", "team_name": "Ferrari"}
                        ]
                        """)));
        server.stubFor(get(urlPathEqualTo("/drivers"))
                .withQueryParam("session_key", equalTo(String.valueOf(IMOLA_SESSION_KEY)))
                .willReturn(serverError()));

        server.stubFor(get(urlPathEqualTo("/location"))
                .withQueryParam("driver_number", equalTo("1"))
                .willReturn(okJson("""
                        [
                          {"date": "2024-05-26T13:00:00.143000+00:00", "session_key": 9523, "x": 10, "y": 20, "z": 0, "driver_number": 1},
                          {"date": "2024-05-26T13:00:00.400000+00:00", "session_key": 9523, "x": 11, "y": 21, "z": 0, "driver_number": 1}
                        ]
                        """)));
        server.stubFor(get(urlPathEqualTo("/location"))
                .withQueryParam("driver_number", equalTo("16"))
                .willReturn(okJson("""
                        [
                          {"date": "2024-05-26T13:00:00.200000+00:00", "session_key": 9523, "x": -5, "y": 7, "z": 0, "driver_number": 16}
                        ]
                        """)));

        return server;
    }

    @Bean
    public DynamicPropertyRegistrar openF1Properties(WireMockServer openF1Server) {
        return registry -> {
            registry.add("openf1.base-url", openF1Server::baseUrl);
            registry.add("openf1.request-delay-ms", () -> "0");
        };
    }

    private static String session(int sessionKey, String dateStart) {
        return """
                [{"session_key": %d, "session_name": "Race", "date_start": "%s", "year": 2024}]
                """.formatted(sessionKey, dateStart);
    }
}
