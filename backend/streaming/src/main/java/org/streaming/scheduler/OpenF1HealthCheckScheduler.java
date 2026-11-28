package org.streaming.scheduler;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(name = "app.scheduling.openf1-health.enabled", havingValue = "true", matchIfMissing = true)
public class OpenF1HealthCheckScheduler {
    private static final Logger log = LoggerFactory.getLogger(OpenF1HealthCheckScheduler.class);

    public enum Status {
        UP, SLOW, DOWN
    }

    private final RestClient restClient;
    private final Duration slowThreshold;
    private volatile Status lastStatus;

    @Autowired
    public OpenF1HealthCheckScheduler(RestClient.Builder restClientBuilder,
                                      @Value("${openf1.base-url:https://api.openf1.org/v1}") String baseUrl,
                                      @Value("${app.scheduling.openf1-health.timeout:5s}") Duration timeout,
                                      @Value("${app.scheduling.openf1-health.slow-threshold:2s}") Duration slowThreshold) {
        this(restClientBuilder
                        .baseUrl(baseUrl)
                        .requestFactory(requestFactory(timeout))
                        .build(),
                slowThreshold);
    }

    OpenF1HealthCheckScheduler(RestClient restClient, Duration slowThreshold) {
        this.restClient = restClient;
        this.slowThreshold = slowThreshold;
    }

    @Scheduled(
            fixedDelayString = "${app.scheduling.openf1-health.fixed-delay:1m}",
            initialDelayString = "${app.scheduling.openf1-health.initial-delay:30s}"
    )
    public void checkOpenF1() {
        long start = System.nanoTime();
        Status status;
        String error = null;

        try {
            restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sessions")
                            .queryParam("session_key", "latest")
                            .build())
                    .retrieve()
                    .toBodilessEntity();
            var latency = Duration.ofNanos(System.nanoTime() - start);
            status = latency.compareTo(slowThreshold) > 0 ? Status.SLOW : Status.UP;
        } catch (RestClientException e) {
            status = Status.DOWN;
            error = e.getMessage();
        }

        logResult(status, Duration.ofNanos(System.nanoTime() - start).toMillis(), error);
        lastStatus = status;
    }

    public Status getLastStatus() {
        return lastStatus;
    }

    private void logResult(Status status, long latencyMs, String error) {
        if (status == lastStatus) {
            log.debug("OpenF1 health check: {} in {} ms", status, latencyMs);
            return;
        }

        switch (status) {
            case UP -> log.info("OpenF1 health check: API is UP ({} ms), previous status {}", latencyMs, lastStatus);
            case SLOW -> log.warn("OpenF1 health check: API is SLOW ({} ms, threshold {} ms)",
                    latencyMs, slowThreshold.toMillis());
            case DOWN -> log.warn("OpenF1 health check: API is DOWN after {} ms: {}", latencyMs, error);
        }
    }

    private static SimpleClientHttpRequestFactory requestFactory(Duration timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }
}
