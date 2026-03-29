package com.example.wallet.coincap.service;

import java.math.BigDecimal;

public interface CoinCapService {

    BigDecimal getPriceBySymbol(String symbol);

}
