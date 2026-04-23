package com.stockengine.stock_market.dto;

import java.util.List;

public record WalletResponseDto(
        String id,
        List<StockDto> stocks
) {}
