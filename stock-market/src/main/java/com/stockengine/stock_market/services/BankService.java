package com.stockengine.stock_market.services;

import com.stockengine.stock_market.dto.BankStateDto;
import com.stockengine.stock_market.dto.StockDto;
import com.stockengine.stock_market.model.BankStock;
import com.stockengine.stock_market.repositories.BankStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BankService {
    private final BankStockRepository bankStockRepository;

    public BankService(BankStockRepository bankStockRepository) {
        this.bankStockRepository = bankStockRepository;
    }

    public BankStateDto getBankState(){
        return new BankStateDto(bankStockRepository.findAll().stream()
                .map(s -> new StockDto(s.getName(), s.getQuantity()))
                .collect(Collectors.toList()));
    }

    @Transactional
    public void setBankState(BankStateDto stateDTO) {

        Map<String, BankStock> existingStocks = bankStockRepository.findAll().stream()
                .collect(Collectors.toMap(BankStock::getName, stock -> stock));

        List<BankStock> toSave = new ArrayList<>();

        Set<String> newStocks = stateDTO.stocks().stream()
                .map(StockDto::name)
                .collect(Collectors.toSet());

        for (StockDto stock : stateDTO.stocks()) {
            BankStock existingStock = existingStocks.get(stock.name());

            if (existingStock == null) {
                toSave.add(new BankStock(stock.name(), stock.quantity()));
            } else {
                existingStock.setQuantity(stock.quantity());
                toSave.add(existingStock);
            }
        }

        List<BankStock> toRemove = existingStocks.values().stream()
                .filter(existingStock -> !newStocks.contains(existingStock.getName()))
                .collect(Collectors.toList());

        bankStockRepository.deleteAll(toRemove);
        bankStockRepository.saveAll(toSave);
    }
}
