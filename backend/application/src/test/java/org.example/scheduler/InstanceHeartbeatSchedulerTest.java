package org.example.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class InstanceHeartbeatSchedulerTest {

    @Test
    void instanceId_ShouldUseActualPort_WhenServerStarted() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("server.port", "8080")
                .withProperty("local.server.port", "8081");

        InstanceHeartbeatScheduler scheduler = new InstanceHeartbeatScheduler(environment);

        assertThat(scheduler.instanceId()).endsWith(":8081");
    }

    @Test
    void instanceId_ShouldFallBackToConfiguredPort() {
        MockEnvironment environment = new MockEnvironment().withProperty("server.port", "9090");

        InstanceHeartbeatScheduler scheduler = new InstanceHeartbeatScheduler(environment);

        assertThat(scheduler.instanceId()).endsWith(":9090");
    }

    @Test
    void formatUptime_ShouldShowDaysHoursMinutesSeconds() {
        Duration uptime = Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4);

        assertThat(InstanceHeartbeatScheduler.formatUptime(uptime)).isEqualTo("1d 02h 03m 04s");
    }
}
