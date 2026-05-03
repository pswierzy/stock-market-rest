package market

import (
	"net/http"
	"os"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Server struct {
	db *pgxpool.Pool
}

func NewServer(db *pgxpool.Pool) *Server {
	return &Server{db: db}
}

func (s *Server) RegisterRoutes(mux *http.ServeMux) {
	mux.HandleFunc("POST /wallets/{wallet_id}/stocks/{stock_name}", s.HandleTrade)
	mux.HandleFunc("GET /wallets/{wallet_id}", s.HandleGetWallet)
	mux.HandleFunc("GET /wallets/{wallet_id}/stocks/{stock_name}", s.HandleGetWalletStock)
	mux.HandleFunc("GET /stocks", s.HandleGetBankStocks)
	mux.HandleFunc("POST /stocks", s.HandleSetBankStocks)
	mux.HandleFunc("GET /log", s.HandleGetLogs)
	
	mux.HandleFunc("POST /chaos", func(w http.ResponseWriter, r *http.Request) {
		os.Exit(1)
	})
	
	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})
}