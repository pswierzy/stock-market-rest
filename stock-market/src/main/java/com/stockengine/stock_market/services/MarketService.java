package com.stockengine.stock_market.services;

import com.stockengine.stock_market.TransactionType;
import com.stockengine.stock_market.dto.StockDto;
import com.stockengine.stock_market.dto.WalletResponse;
import com.stockengine.stock_market.exceptions.StockNotFoundException;
import com.stockengine.stock_market.exceptions.WalletNotFoundException;
import com.stockengine.stock_market.model.BankStock;
import com.stockengine.stock_market.model.Wallet;
import com.stockengine.stock_market.repositories.BankStockRepository;
import com.stockengine.stock_market.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarketService {
    private final BankStockRepository bankStockRepository;
    private final WalletRepository walletRepository;

    public MarketService(BankStockRepository bankStockRepository, WalletRepository walletRepository) {
        this.bankStockRepository = bankStockRepository;
        this.walletRepository = walletRepository;
    }

    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    DataIntegrityViolationException.class // for 2 instances creating 2 new Wallets
            },
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void trade(String walletId, String stockName, TransactionType transactionType) {
        BankStock bankStock = bankStockRepository.findById(stockName)
                .orElseThrow(() -> new StockNotFoundException("Stock not found: " + stockName));

        Wallet wallet = walletRepository.findById(walletId)
                .orElseGet(() -> new Wallet(walletId));

        if (transactionType == TransactionType.SELL) {
            bankStock.increaseQuantity(1);
            wallet.removeStock(stockName, 1);
        } else if (transactionType == TransactionType.BUY) {
            bankStock.decreaseQuantity(1);
            wallet.addStock(stockName, 1);
        } else {
            throw new IllegalStateException("Unknown transaction type: " + transactionType);
        }

        walletRepository.save(wallet);
        bankStockRepository.save(bankStock);
    }

    public WalletResponse getWallet(String walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        List<StockDto> stocks = wallet.getStocks().entrySet().stream()
                .map(entry -> new StockDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return new WalletResponse(walletId, stocks);
    }

    public int getWalletStock(String walletId, String stockName) {
        return walletRepository.findById(walletId)
                .map(w -> w.getStocks().getOrDefault(stockName, 0))
                .orElse(0);
    }
}
