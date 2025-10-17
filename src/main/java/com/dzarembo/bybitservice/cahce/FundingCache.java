package com.dzarembo.bybitservice.cahce;

import com.dzarembo.bybitservice.model.FundingRate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FundingCache {
    private final Map<String, FundingRate> cache = new ConcurrentHashMap<>();

    public void putAll(Map<String, FundingRate> data) {
        cache.clear();
        cache.putAll(data);
    }

    public FundingRate get(String symbol) {
        return cache.get(symbol);
    }

    public Collection<FundingRate> getAll() {
        return cache.values();
    }
}
