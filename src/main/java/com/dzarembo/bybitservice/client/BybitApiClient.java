package com.dzarembo.bybitservice.client;

import com.dzarembo.bybitservice.model.FundingRate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Получает funding rates с Bybit API
 *
 */
@Component
public class BybitApiClient {
    private static final String BYBIT_TICKERS = "https://api.bybit.com/v5/market/tickers?category=linear";
    private final RestTemplate rest = new RestTemplate();

    public Map<String, FundingRate> fetchFundingRates() {
        try {
            TickersResponse resp = rest.getForObject(BYBIT_TICKERS, TickersResponse.class);
            if (resp == null || resp.result == null || resp.result.list == null) return Collections.emptyMap();

            return resp.result.list.stream()
                    .collect(Collectors.toMap(
                            t -> t.symbol,
                            t -> new FundingRate(
                                    t.symbol,
                                    parseDoubleSafe(t.fundingRate),
                                    parseLongSafe(t.nextFundingTime)
                            )
                    ));
        } catch (Exception e) {
            System.err.println("Bybit fetch error: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private double parseDoubleSafe(String s) {
        try {
            return s == null || s.isEmpty() ? 0.0 : Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private long parseLongSafe(String s) {
        try {
            return s == null || s.isEmpty() ? 0L : Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }

    // DTOs (inner classes)
    private static class TickersResponse {
        public RespResult result;
    }

    private static class RespResult {
        public List<TickerItem> list;
    }

    private static class TickerItem {
        public String symbol;
        public String fundingRate;       // string representation
        public String nextFundingTime;   // string (ms) in docs
        // other fields ignored
    }
}

