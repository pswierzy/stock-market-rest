package com.stockengine.stock_market.services;

import com.stockengine.stock_market.TransactionType;
import com.stockengine.stock_market.model.BankStock;
import com.stockengine.stock_market.model.Wallet;
import com.stockengine.stock_market.repositories.BankStockRepository;
import com.stockengine.stock_market.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

public class MarketService {
    private final BankStockRepository bankStockRepository;
    private final WalletRepository walletRepository;

    public MarketService(BankStockRepository bankStockRepository, WalletRepository walletRepository) {
        this.bankStockRepository = bankStockRepository;
        this.walletRepository = walletRepository;
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void trade(String walletId, String stockName, TransactionType transactionType) {
        BankStock bankStock = bankStockRepository.findById(stockName)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + stockName));

        Wallet wallet = walletRepository.findById(walletId)
                .orElseGet(() -> new Wallet(walletId));

        if (transactionType == TransactionType.SELL) {
            bankStock.increaseQuantity(1);
            wallet.addStock(stockName, 1);
        } else if (transactionType == TransactionType.BUY) {
            bankStock.decreaseQuantity(1);
            wallet.addStock(stockName, -1);
        } else {
            throw new IllegalStateException("Unknown transaction type: " + transactionType);
        }

        walletRepository.save(wallet);
        bankStockRepository.save(bankStock);
    }
}
