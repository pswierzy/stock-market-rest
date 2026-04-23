package com.stockengine.stock_market;

import com.stockengine.stock_market.dto.*;
import com.stockengine.stock_market.services.BankService;
import com.stockengine.stock_market.services.MarketService;
import org.springframework.web.bind.annotation.*;

@RestController
public class MarketController {
    private final MarketService marketService;
    private final BankService bankService;

    public MarketController(MarketService marketService, BankService bankService) {
        this.marketService = marketService;
        this.bankService = bankService;
    }

    @PostMapping("/wallets/{wallet_id}/stocks/{stock_name}")
    public void trade(@PathVariable("wallet_id") String walletId, @PathVariable("stock_name") String stockName, @RequestBody TradeRequestDto request) {
        marketService.trade(walletId, stockName, request.type());
    }

    @GetMapping("/wallets/{wallet_id}")
    public WalletResponseDto getWallet(@PathVariable("wallet_id") String walletId) {
        return marketService.getWallet(walletId);
    }

    @GetMapping("/wallets/{wallet_id}/stocks/{stock_name}")
    public int getWalletStock(@PathVariable("wallet_id") String walletId, @PathVariable("stock_name") String stockName) {
        return marketService.getWalletStock(walletId, stockName);
    }

    @GetMapping("/stocks")
    public BankStateDto getStocks() {
        return bankService.getBankState();
    }

    @PostMapping("/stocks")
    public void setStocks(@RequestBody BankStateDto stocks) {
        bankService.setBankState(stocks);
    }

    @GetMapping("/log")
    public LogResponseDto getLogs() {
        return marketService.getLogs();
    }

    @PostMapping("/chaos")
    public void chaos() {
        System.exit(0);
    }
}
