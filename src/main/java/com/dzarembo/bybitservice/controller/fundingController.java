package com.dzarembo.bybitservice.controller;

import com.dzarembo.bybitservice.cahce.FundingCache;
import com.dzarembo.bybitservice.model.FundingRate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/bybit/funding")
@RequiredArgsConstructor
public class fundingController {
    private final FundingCache fundingCache;


    @GetMapping
    public Collection<FundingRate> getAll() {
        return fundingCache.getAll();
    }

    @GetMapping("/{symbol}")
    public FundingRate getBySymbol(@PathVariable String symbol) {
        return fundingCache.get(symbol.toUpperCase());
    }
}
