package org.example.scheduler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class JvmMetricsScheduler {
    private static final Logger log = LoggerFactory.getLogger(JvmMetricsScheduler.class);
    private static final long MB = 1024 * 1024;

    record JvmMetrics(long heapUsedMb,
                      long heapCommittedMb,
                      long heapMaxMb,
                      int threadCount,
                      int peakThreadCount,
                      double processCpuLoad,
                      int availableProcessors) {
    }

    @Scheduled(
            fixedDelayString = "${app.scheduling.jvm-metrics.fixed-delay:15s}",
            initialDelayString = "${app.scheduling.jvm-metrics.initial-delay:15s}"
    )
    public void logJvmMetrics() {
        JvmMetrics metrics = collect();

        log.info("JVM metrics: heap {}/{} MB (max {} MB), threads {} (peak {}), process CPU {}%, processors {}",
                metrics.heapUsedMb(), metrics.heapCommittedMb(), metrics.heapMaxMb(),
                metrics.threadCount(), metrics.peakThreadCount(),
                String.format("%.1f", metrics.processCpuLoad() * 100), metrics.availableProcessors());
    }

    JvmMetrics collect() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();

        double cpuLoad = os instanceof com.sun.management.OperatingSystemMXBean hotspotOs
                ? hotspotOs.getProcessCpuLoad()
                : -1;

        return new JvmMetrics(
                heap.getUsed() / MB,
                heap.getCommitted() / MB,
                heap.getMax() / MB,
                threads.getThreadCount(),
                threads.getPeakThreadCount(),
                cpuLoad,
                os.getAvailableProcessors()
        );
    }
}
