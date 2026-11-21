package org.streaming.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RacesCacheCleanUpScheduler {
    private static final Logger log = LoggerFactory.getLogger(RacesCacheCleanUpScheduler.class);
    private final CacheManager cacheManager;

    public RacesCacheCleanUpScheduler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(
            cron = "${app.scheduling.cache-cleanup.cron:0 0 0 * * *}",
            zone = "Europe/Kyiv"
    )
    public void cleanRacesBuffer() {
        log.info("Starting scheduled midnight cleanup of races buffer cache");

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cache '{}' successfully cleared!", cacheName);
            }
        });
    }
}
