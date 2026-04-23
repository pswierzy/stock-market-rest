package com.stockengine.stock_market.model;

import com.stockengine.stock_market.exceptions.InsufficientStockException;
import jakarta.persistence.*;

import java.util.HashMap;

import java.util.Map;


@Entity
public class Wallet {
    @Id
    private String id;

    @Version
    private Long version;

    @ElementCollection
    @CollectionTable(name = "wallet_stocks", joinColumns = @JoinColumn(name = "wallet_id"))
    @MapKeyColumn(name = "stock_name")
    @Column(name = "quantity")
    private Map<String, Integer> stocks = new HashMap<>();

    public Wallet() {}
    public Wallet(String id) {
        this.id = id;
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public Map<String, Integer> getStocks() { return stocks; }
    public void addStock(String name, Integer quantity) {
        stocks.merge(name, quantity, Integer::sum);
    }
    public void removeStock(String name, Integer quantity) {
        Integer currQuantity = stocks.get(name);

        if (currQuantity == null || currQuantity < quantity) {
            throw new InsufficientStockException("Not enough stocks in wallet");
        }

        if (currQuantity.equals(quantity)) {
            stocks.remove(name);
        } else {
            stocks.put(name, currQuantity - quantity);
        }
    }

    public Long getVersion() { return version; }
}
