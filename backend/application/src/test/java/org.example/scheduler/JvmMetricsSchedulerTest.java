package org.example.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.example.scheduler.JvmMetricsScheduler.JvmMetrics;
import org.junit.jupiter.api.Test;

class JvmMetricsSchedulerTest {

    private final JvmMetricsScheduler scheduler = new JvmMetricsScheduler();

    @Test
    void collect_ShouldReadMetricsOfCurrentJvm() {
        JvmMetrics metrics = scheduler.collect();

        assertThat(metrics.heapUsedMb()).isNotNegative();
        assertThat(metrics.heapCommittedMb()).isGreaterThanOrEqualTo(metrics.heapUsedMb());
        assertThat(metrics.threadCount()).isPositive();
        assertThat(metrics.peakThreadCount()).isGreaterThanOrEqualTo(metrics.threadCount());
        assertThat(metrics.availableProcessors()).isPositive();
    }

    @Test
    void logJvmMetrics_ShouldNotFail() {
        assertThatCode(scheduler::logJvmMetrics).doesNotThrowAnyException();
    }
}
