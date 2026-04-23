package com.stockengine.stock_market.dto;

import com.stockengine.stock_market.TransactionType;

public record TradeRequestDto(
        TransactionType type
) {}