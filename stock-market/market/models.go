package market

type TradeType string

const (
	Buy  TradeType = "buy"
	Sell TradeType = "sell"
)

func (t TradeType) IsValid() bool {
	switch t {
	case Buy, Sell:
		return true
	}
	return false
}

type Stock struct {
	Name     string `json:"name"`
	Quantity int    `json:"quantity"`
}

type TradeRequest struct {
	Type TradeType `json:"type"`
}

type WalletResponse struct {
	ID     string  `json:"id"`
	Stocks []Stock `json:"stocks"`
}

type BankState struct {
	Stocks []Stock `json:"stocks"`
}

type LogEntry struct {
	Type      TradeType `json:"type"`
	WalletID  string    `json:"wallet_id"`
	StockName string    `json:"stock_name"`
}

type LogResponse struct {
	Log []LogEntry `json:"log"`
}