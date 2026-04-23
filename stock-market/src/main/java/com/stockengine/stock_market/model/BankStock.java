package com.stockengine.stock_market.model;

import com.stockengine.stock_market.exceptions.InsufficientStockException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class BankStock {
    @Id
    private String name;

    private Integer quantity;

    @Version
    private Long version;

    public BankStock() {}
    public BankStock(String name, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public void increaseQuantity(Integer quantity) { this.quantity += quantity; }
    public void decreaseQuantity(int amount) {
        if (this.quantity < amount) {
            throw new InsufficientStockException("Not enough stocks in bank");
        }
        this.quantity -= amount;
    }

    public Long getVersion() { return version; }
}
