package com.dzarembo.bybitservice.client;

import com.dzarembo.bybitservice.model.FundingRate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Клиент для получения funding rates и интервалов с Bybit API.
 */
@Component
public class BybitApiClient {
    private static final String BYBIT_TICKERS = "https://api.bybit.com/v5/market/tickers?category=linear";
    private static final String BYBIT_INSTRUMENTS = "https://api.bybit.com/v5/market/instruments-info?category=linear";
    private final RestTemplate rest = new RestTemplate();

    public Map<String, FundingRate> fetchFundingRates() {
        try {
            // 1️⃣ fundingRate из /tickers
            TickersResponse tickersResp = rest.getForObject(BYBIT_TICKERS, TickersResponse.class);
            if (tickersResp == null || tickersResp.result == null || tickersResp.result.list == null) {
                System.err.println("⚠️ tickersResp.result.list = null");
                return Collections.emptyMap();
            }

            Map<String, FundingRate> result = tickersResp.result.list.stream()
                    .collect(Collectors.toMap(
                            t -> t.symbol,
                            t -> new FundingRate(
                                    t.symbol,
                                    parseDoubleSafe(t.fundingRate),
                                    parseLongSafe(t.nextFundingTime),
                                    0
                            )
                    ));

            System.out.println("✅ Получено fundingRates: " + result.size());

            // 2️⃣ fundingInterval из /instruments-info (все страницы)
            Map<String, Integer> fundingIntervals = fetchFundingIntervals();

            // 3️⃣ Объединяем по символу
            result.forEach((symbol, fr) -> {
                Integer interval = fundingIntervals.get(symbol);
                fr.setFundingIntervalHours(interval != null ? interval : 0);
            });

            // 4️⃣ Отладка — вывести пары с нулевым интервалом
            long zeroCount = result.values().stream().filter(f -> f.getFundingIntervalHours() == 0).count();
            System.out.println("⚠️ Пары с fundingInterval=0: " + zeroCount);

            return result;

        } catch (Exception e) {
            System.err.println("Bybit fetch error: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Integer> fetchFundingIntervals() {
        Map<String, Integer> result = new HashMap<>();
        String cursor = null;
        int page = 1;

        try {
            do {
                String url = BYBIT_INSTRUMENTS;
                if (cursor != null) {
                    url += "&cursor=" + cursor;
                }

                System.out.println("➡️ Загружаем страницу #" + page + ": " + url);
                InstrumentsResponse resp = rest.getForObject(url, InstrumentsResponse.class);

                if (resp == null) {
                    System.err.println("⚠️ resp == null");
                    break;
                }
                if (resp.result == null) {
                    System.err.println("⚠️ resp.result == null");
                    break;
                }
                if (resp.result.list == null) {
                    System.err.println("⚠️ resp.result.list == null");
                    break;
                }

                System.out.println("📦 Страница #" + page + " содержит " + resp.result.list.size() + " инструментов");

                for (InstrumentItem item : resp.result.list) {
                    int minutes = item.fundingInterval != null ? item.fundingInterval : 0;
                    int hours = minutes > 0 ? minutes / 60 : 0;
                    result.put(item.symbol, hours);
                }

                // Сохраняем следующий курсор
                cursor = resp.result.nextPageCursor;
                if (cursor == null || cursor.isEmpty()) {
                    System.out.println("⏹ Конец страниц (nextPageCursor пуст)");
                    break;
                }

                page++;
                Thread.sleep(100); // лёгкая задержка чтобы не ловить rate-limit

            } while (true);

            System.out.println("📊 Всего fundingIntervals: " + result.size());
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке fundingInterval: " + e.getMessage());
        }

        return result;
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

    // DTOs для /tickers
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TickersResponse {
        public RespResult result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RespResult {
        public List<TickerItem> list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TickerItem {
        public String symbol;
        public String fundingRate;
        public String nextFundingTime;
    }

    // DTOs для /instruments-info
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class InstrumentsResponse {
        public InstrumentsResult result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class InstrumentsResult {
        public List<InstrumentItem> list;
        public String nextPageCursor;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class InstrumentItem {
        public String symbol;
        public Integer fundingInterval; // в минутах
    }
}
