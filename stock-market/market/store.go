package market

import (
	"context"
	"errors"

	"github.com/jackc/pgx/v5"
)


var (
	ErrStockNotFound = errors.New("stock not found")
	ErrInsufficient  = errors.New("insufficient stock")
)

func (s *Server) Trade(ctx context.Context, walletID string, stockName string, tradeType TradeType) error {
	tx, err := s.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	var bankQty int
	err = tx.QueryRow(ctx, "SELECT quantity FROM bank_stocks WHERE name = $1 FOR UPDATE", stockName).Scan(&bankQty)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return ErrStockNotFound
		}
		return err
	}

	var walletQty int
	err = tx.QueryRow(ctx, "SELECT quantity FROM wallet_stocks WHERE wallet_id = $1 AND stock_name = $2 FOR UPDATE", walletID, stockName).Scan(&walletQty)
	if err != nil && !errors.Is(err, pgx.ErrNoRows) {
		return err
	}

	if tradeType == Buy {
		if bankQty < 1 {
			return ErrInsufficient
		}
		bankQty--
		walletQty++
	} else if tradeType == Sell {
		if walletQty < 1 {
			return ErrInsufficient
		}
		bankQty++
		walletQty--
	}

	_, err = tx.Exec(ctx, "UPDATE bank_stocks SET quantity = $1 WHERE name = $2", bankQty, stockName)
	if err != nil { return err }

	_, err = tx.Exec(ctx, `
		INSERT INTO wallet_stocks (wallet_id, stock_name, quantity) 
		VALUES ($1, $2, $3) 
		ON CONFLICT (wallet_id, stock_name) 
		DO UPDATE SET quantity = EXCLUDED.quantity`, 
		walletID, stockName, walletQty)
	if err != nil { return err }

	_, err = tx.Exec(ctx, "INSERT INTO audit_log (type, wallet_id, stock_name) VALUES ($1, $2, $3)", tradeType, walletID, stockName)
	if err != nil { return err }

	return tx.Commit(ctx)
}

func (s *Server) GetWallet(ctx context.Context, walletID string) (WalletResponse, error) {
	stocks := make([]Stock, 0)
	rows, err := s.db.Query(ctx, "SELECT stock_name, quantity FROM wallet_stocks WHERE wallet_id = $1", walletID)

	if err != nil {
		return WalletResponse{}, err
	}
	defer rows.Close()

	for rows.Next() {
		var s Stock
		if err := rows.Scan(&s.Name, &s.Quantity); err != nil {
			return WalletResponse{}, err
		}
		stocks = append(stocks, s)
	}

	return WalletResponse{ID: walletID, Stocks: stocks}, nil
}

func (s *Server) GetWalletStock(ctx context.Context, walletID string, stockName string) (int, error) {
	var quantity int
	err := s.db.QueryRow(ctx, "SELECT quantity FROM wallet_stocks WHERE wallet_id = $1 AND stock_name = $2", walletID, stockName).Scan(&quantity)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return 0, nil
		}
		return 0, err
	}
	return quantity, nil
}

func (s *Server) GetBankStocks(ctx context.Context) (BankState, error) {
	stocks := make([]Stock, 0)
	rows, err := s.db.Query(ctx, "SELECT name, quantity FROM bank_stocks")
	if err != nil {
		return BankState{}, err
	}
	defer rows.Close()
	for rows.Next() {
		var s Stock
		if err := rows.Scan(&s.Name, &s.Quantity); err != nil {
			return BankState{}, err
		}
		stocks = append(stocks, s)
	}
	return BankState{Stocks: stocks}, nil
}

func (s *Server) SetBankStocks(ctx context.Context, state BankState) error {
	tx, err := s.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)
	query := `
		INSERT INTO bank_stocks (name, quantity) 
		VALUES ($1, $2) 
		ON CONFLICT (name) 
		DO UPDATE SET quantity = EXCLUDED.quantity
	`

	for _, stock := range state.Stocks {
		if _, err := tx.Exec(ctx, query, stock.Name, stock.Quantity); err != nil {
			return err
		}
	}
	return tx.Commit(ctx)
}

func (s *Server) GetLogs(ctx context.Context) (LogResponse, error) {
	logs := make([]LogEntry, 0)
	rows, err := s.db.Query(ctx, "SELECT type, wallet_id, stock_name FROM audit_log ORDER BY id ASC")
	if err != nil {
		return LogResponse{}, err
	}
	defer rows.Close()

	for rows.Next() {
		var entry LogEntry
		if err := rows.Scan(&entry.Type, &entry.WalletID, &entry.StockName); err != nil {
			return LogResponse{}, err
		}
		logs = append(logs, entry)
	}
	return LogResponse{Log: logs}, nil
}