package com.stockengine.stock_market.services;

import com.stockengine.stock_market.model.BankStock;
import com.stockengine.stock_market.repositories.BankStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankService {
    private final BankStockRepository bankStockRepository;

    public BankService(BankStockRepository bankStockRepository) {
        this.bankStockRepository = bankStockRepository;
    }

    @Transactional
    public void setBankState(List<BankStock> bankStocks) {
        bankStockRepository.deleteAll();
        bankStockRepository.saveAll(bankStocks);
    }

    public List<BankStock> getBankStock() { return bankStockRepository.findAll(); }
}
