package market

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"
)

func (s *Server) HandleTrade(w http.ResponseWriter, r *http.Request) {
	walletID := r.PathValue("wallet_id")
	stockName := r.PathValue("stock_name")

	var req TradeRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}

	if !req.Type.IsValid() {
		http.Error(w, "invalid trade type", http.StatusBadRequest)
		return
	}

	err := s.Trade(r.Context(), walletID, stockName, req.Type)
	if err != nil {
		if errors.Is(err, ErrStockNotFound) {
			http.Error(w, err.Error(), http.StatusNotFound)
			return
		}
		if errors.Is(err, ErrInsufficient) {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func (s *Server) HandleGetWallet(w http.ResponseWriter, r *http.Request) {
	walletID := r.PathValue("wallet_id")

	wallet, err := s.GetWallet(r.Context(), walletID)
	if err != nil {
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(wallet)
}

func (s *Server) HandleGetWalletStock(w http.ResponseWriter, r *http.Request) {
	walletID := r.PathValue("wallet_id")
	stockName := r.PathValue("stock_name")

	quantity, err := s.GetWalletStock(r.Context(), walletID, stockName)
	if err != nil {
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/plain")
	w.Write([]byte(strconv.Itoa(quantity)))
}

func (s *Server) HandleGetBankStocks(w http.ResponseWriter, r *http.Request) {
	stocks, err := s.GetBankStocks(r.Context())
	if err != nil {
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(stocks)
}

func (s *Server) HandleSetBankStocks(w http.ResponseWriter, r *http.Request) {
	var state BankState
	if err := json.NewDecoder(r.Body).Decode(&state); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}

	if err := s.SetBankStocks(r.Context(), state); err != nil {
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusOK)
}

func (s *Server) HandleGetLogs(w http.ResponseWriter, r *http.Request) {
	logs, err := s.GetLogs(r.Context())
	if err != nil {
		http.Error(w, "internal server error", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(logs)
}