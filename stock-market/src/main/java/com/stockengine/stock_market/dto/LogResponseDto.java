package com.stockengine.stock_market.dto;

import java.util.List;

public record LogResponseDto(
        List<LogEntryDto> log
) {}
