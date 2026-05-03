package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"

	"stock-market/market"

	"github.com/jackc/pgx/v5/pgxpool"
)

func initDB(pool *pgxpool.Pool) {
	query := `
	CREATE TABLE IF NOT EXISTS bank_stocks (
		name VARCHAR(255) PRIMARY KEY,
		quantity INT NOT NULL
	);
	CREATE TABLE IF NOT EXISTS wallet_stocks (
		wallet_id VARCHAR(255),
		stock_name VARCHAR(255),
		quantity INT NOT NULL,
		PRIMARY KEY (wallet_id, stock_name)
	);
	CREATE TABLE IF NOT EXISTS audit_log (
		id SERIAL PRIMARY KEY,
		type VARCHAR(10) NOT NULL,
		wallet_id VARCHAR(255) NOT NULL,
		stock_name VARCHAR(255) NOT NULL
	);
	`
	_, err := pool.Exec(context.Background(), query)
	if err != nil {
		log.Fatalf("Failed to initialize database schema: %v\n", err)
	}
}

func main() {
	port := "8080"

	connStr := fmt.Sprintf("postgresql://%s:%s@%s:%s/%s?sslmode=disable",
		os.Getenv("DB_USER"), os.Getenv("DB_PASS"), 
		os.Getenv("DB_HOST"), os.Getenv("DB_PORT"), 
		os.Getenv("DB_NAME"))

	pool, err := pgxpool.New(context.Background(), connStr)
	if err != nil {
		log.Fatalf("Unable to connect to database: %v", err)
	}
	defer pool.Close()

	initDB(pool)

	srv := market.NewServer(pool)
	mux := http.NewServeMux()
	srv.RegisterRoutes(mux)
	
	log.Printf("Server is running on port %s\n", port)
	if err := http.ListenAndServe(":"+port, mux); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}