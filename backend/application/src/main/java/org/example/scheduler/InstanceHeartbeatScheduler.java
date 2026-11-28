package org.example.scheduler;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InstanceHeartbeatScheduler {
    private static final Logger log = LoggerFactory.getLogger(InstanceHeartbeatScheduler.class);

    private final Environment environment;
    private final String hostName;

    public InstanceHeartbeatScheduler(Environment environment) {
        this.environment = environment;
        this.hostName = resolveHostName();
    }

    @Scheduled(
            fixedRateString = "${app.scheduling.heartbeat.fixed-rate:30s}",
            initialDelayString = "${app.scheduling.heartbeat.initial-delay:10s}"
    )
    public void sendHeartbeat() {
        Duration uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());

        log.info("Heartbeat: instance {} is alive, uptime {}", instanceId(), formatUptime(uptime));
    }

    String instanceId() {
        String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "8080"));
        return hostName + ":" + port;
    }

    static String formatUptime(Duration uptime) {
        return String.format("%dd %02dh %02dm %02ds",
                uptime.toDays(), uptime.toHoursPart(), uptime.toMinutesPart(), uptime.toSecondsPart());
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException _) {
            return "unknown-host";
        }
    }
}
