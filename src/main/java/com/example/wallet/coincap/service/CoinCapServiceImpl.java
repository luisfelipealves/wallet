package com.example.wallet.coincap.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.wallet.coincap.model.PriceResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinCapServiceImpl implements CoinCapService {

    private final RestClient coinCapClient;

    @Value("${coincap.price-by-symbol-path}")
    private String priceBySymbolPath;

    @Override
    public BigDecimal getPriceBySymbol(String symbol) {
        try {
            PriceResponse response = coinCapClient.get()
                    .uri(priceBySymbolPath, symbol)
                    .retrieve()
                    .body(PriceResponse.class);

            if (response != null && response.data() != null && !response.data().isEmpty()) {
                String price = response.data().get(0);
                if (price != null && !price.equals("null")) {
                    return new BigDecimal(price);
                }
            }
        } catch (RestClientException e) {
            log.warn("Error fetching price for asset '{}': {}", symbol, e.getMessage());
        }

        return null;
    }

}
