package com.dzarembo.bybitservice.scheduler;

import com.dzarembo.bybitservice.cahce.FundingCache;
import com.dzarembo.bybitservice.client.BybitApiClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BybitUpdater {
    private static final Logger log = LoggerFactory.getLogger(BybitUpdater.class);
    private final FundingCache cache;
    private final BybitApiClient bybitApiClient;


    @Scheduled(initialDelay = 5000, fixedRate = 60_000) // каждая минута
    public void updateFundingRates() {
        log.info("Updating Bybit funding rates...");
        cache.putAll(bybitApiClient.fetchFundingRates());
        log.info("Bybit cache updated. Cached pairs: {}", cache.getAll().size());
    }

}
