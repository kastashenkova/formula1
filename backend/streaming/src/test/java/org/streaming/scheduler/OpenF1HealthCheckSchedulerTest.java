package org.streaming.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.streaming.scheduler.OpenF1HealthCheckScheduler.Status;

class OpenF1HealthCheckSchedulerTest {

    private static final String BASE_URL = "https://openf1.test/v1";
    private static final String HEALTH_URL = BASE_URL + "/sessions?session_key=latest";

    private MockRestServiceServer server;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    @Test
    void checkOpenF1_ShouldReportUp_WhenApiResponds() {
        OpenF1HealthCheckScheduler scheduler = new OpenF1HealthCheckScheduler(restClient, Duration.ofSeconds(2));
        server.expect(requestTo(HEALTH_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        scheduler.checkOpenF1();

        assertThat(scheduler.getLastStatus()).isEqualTo(Status.UP);
        server.verify();
    }

    @Test
    void checkOpenF1_ShouldReportSlow_WhenLatencyExceedsThreshold() {
        OpenF1HealthCheckScheduler scheduler = new OpenF1HealthCheckScheduler(restClient, Duration.ZERO);
        server.expect(requestTo(HEALTH_URL))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        scheduler.checkOpenF1();

        assertThat(scheduler.getLastStatus()).isEqualTo(Status.SLOW);
    }

    @Test
    void checkOpenF1_ShouldReportDown_WhenApiReturnsServerError() {
        OpenF1HealthCheckScheduler scheduler = new OpenF1HealthCheckScheduler(restClient, Duration.ofSeconds(2));
        server.expect(requestTo(HEALTH_URL)).andRespond(withServerError());

        scheduler.checkOpenF1();

        assertThat(scheduler.getLastStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void checkOpenF1_ShouldReportDown_WhenApiIsUnreachable() {
        OpenF1HealthCheckScheduler scheduler = new OpenF1HealthCheckScheduler(restClient, Duration.ofSeconds(2));
        server.expect(requestTo(HEALTH_URL)).andRespond(withException(new SocketTimeoutException("timeout")));

        scheduler.checkOpenF1();

        assertThat(scheduler.getLastStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void checkOpenF1_ShouldRecover_WhenApiComesBack() {
        OpenF1HealthCheckScheduler scheduler = new OpenF1HealthCheckScheduler(restClient, Duration.ofSeconds(2));
        server.expect(requestTo(HEALTH_URL)).andRespond(withServerError());
        server.expect(requestTo(HEALTH_URL)).andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        scheduler.checkOpenF1();
        scheduler.checkOpenF1();

        assertThat(scheduler.getLastStatus()).isEqualTo(Status.UP);
        server.verify();
    }
}
