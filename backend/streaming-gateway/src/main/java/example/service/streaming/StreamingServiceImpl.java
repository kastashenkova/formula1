package example.service.streaming;

import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// realization attempt: to change in the future
@Service
public class StreamingServiceImpl implements StreamingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final CacheManager cacheManager;
    private int currentLap = 1;
    private int currentTickIndex = 0;

    public StreamingServiceImpl(SimpMessagingTemplate messagingTemplate,
                                CacheManager cacheManager) {
        this.messagingTemplate = messagingTemplate;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 1000)
    @Override
    public void streamTelemetry() {
        Long currentRaceId = 1L;

        Object telemetryData = getNextTickFromCache(currentRaceId);

        if (telemetryData != null) {
            messagingTemplate.convertAndSend("/topic/races/" + currentRaceId, telemetryData);
        }
    }

    private Object getNextTickFromCache(Long raceId) {
        Cache lapsCache = cacheManager.getCache("laps");
        if (lapsCache == null) {
            return null;
        }

        String cacheKey = "race_" + raceId + "_lap_" + currentLap;
        List<?> currentLapData = lapsCache.get(cacheKey, List.class);

        if (currentLapData == null || currentLapData.isEmpty()) {
            return null;
        }

        if (currentTickIndex < currentLapData.size()) {
            Object tickData = currentLapData.get(currentTickIndex);
            currentTickIndex++;
            return tickData;
        } else {
            currentLap++;
            currentTickIndex = 0;

            // TODO: Pre-fetching from the db for the next lap (currentLap + 1)

            return getNextTickFromCache(raceId);
        }
    }
}
