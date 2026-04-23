package com.stockengine.stock_market.repositories;

import com.stockengine.stock_market.model.BankStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankStockRepository extends JpaRepository<BankStock, String> {
}
