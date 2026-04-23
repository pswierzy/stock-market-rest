package com.stockengine.stock_market.dto;

import java.util.List;

public record WalletResponse(String id, List<StockDto> stocks) {}
