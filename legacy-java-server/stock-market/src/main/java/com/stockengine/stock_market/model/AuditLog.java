package com.stockengine.stock_market.model;

import com.stockengine.stock_market.TransactionType;
import jakarta.persistence.*;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    public AuditLog() {
    }

    public AuditLog(TransactionType type, String walletId, String stockName) {
        this.type = type;
        this.walletId = walletId;
        this.stockName = stockName;
    }

    public TransactionType getType() { return type; }
    public String getWalletId() { return walletId; }
    public String getStockName() { return stockName; }
}