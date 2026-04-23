package com.stockengine.stock_market;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransactionType {
    @JsonProperty("buy")
    BUY,

    @JsonProperty("sell")
    SELL
}
