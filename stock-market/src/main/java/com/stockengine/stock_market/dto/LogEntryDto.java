package com.stockengine.stock_market.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockengine.stock_market.TransactionType;

public record LogEntryDto(
   TransactionType type,
   @JsonProperty("wallet_id") String walletId,
   @JsonProperty("stock_name") String stockName
) {}
