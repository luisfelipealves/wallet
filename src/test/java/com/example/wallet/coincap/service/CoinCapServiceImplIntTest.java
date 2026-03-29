package com.example.wallet.coincap.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

@SpringBootTest(classes = { CoinCapServiceImpl.class, CoinCapServiceImplIntTest.TestConfig.class })
@ActiveProfiles("test")
class CoinCapServiceImplIntTest {

    private static MockRestServiceServer mockServer;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public RestClient coinCapClientTest() {
            RestClient.Builder builder = RestClient.builder();
            builder.baseUrl("https://rest.coincap.io/v3")
                    .defaultHeader("Authorization", "Bearer test-token");

            mockServer = MockRestServiceServer.bindTo(builder).build();

            return builder.build();
        }
    }

    @Autowired
    private CoinCapService coinCapService;

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    @Test
    void getPriceBySymbol_ReturnsPrice_WhenResponseIsValid() {
        mockServer.expect(MockRestRequestMatchers.requestTo("https://rest.coincap.io/v3/price/bysymbol/BTC"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {
                            "data": ["50000.0"],
                            "timestamp": 1234567890
                        }
                        """, MediaType.APPLICATION_JSON));

        BigDecimal price = coinCapService.getPriceBySymbol("BTC");

        assertEquals(new BigDecimal("50000.0"), price);
        mockServer.verify();
    }

    @Test
    void getPriceBySymbol_ReturnsNull_WhenDataIsEmpty() {
        mockServer.expect(MockRestRequestMatchers.requestTo("https://rest.coincap.io/v3/price/bysymbol/ETH"))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        {
                            "data": [],
                            "timestamp": 1234567890
                        }
                        """, MediaType.APPLICATION_JSON));

        BigDecimal price = coinCapService.getPriceBySymbol("ETH");

        assertNull(price);
        mockServer.verify();
    }
}
