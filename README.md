# Simplified Stock Market

A highly available RESTful service simulating a simplified stock market environment. The system allows users (wallets) to buy and sell stocks directly from a central Bank acting as the sole liquidity provider.

## Architecture

The solution is designed for High Availability (HA) and robustness under concurrent load.

- **API Gateway / Load Balancer:** Nginx
- **Application Backend:** Go 1.26
- **Database:** PostgreSQL 15
- **Orchestration:** Docker & Docker Compose

### System topology

The environment spins up:

- **1x Nginx Load Balancer** mapped to a dynamic host port.
- **5x API Replicas** running the Go application, hidden behind the load balancer.
- **1x PostgreSQL Instance** maintaining state safely using row-level locking.

## Getting Started

### Prerequisites

- Docker & Docker Compose plugin
- Git

### Starting the Application

The application can be started using a single command depending on your operating system. The `PORT` parameter dictates where the API will be exposed on your `localhost`.

**Linux / macOS:**

```bash
./start.sh <PORT>
# Example: ./start.sh 8080
```

**Windows**

```bash
./start <PORT>
# Example: ./start 8080
```

The script will automatically build the images, start the cluster, and wait for the healthchecks to pass. Upon success, the API will be available at http://localhost:<PORT>.

## API Endpoints

| Method | Endpoint                                   | Description                                                     | Expected HTTP Status                                                 |
| :----- | :----------------------------------------- | :-------------------------------------------------------------- | :------------------------------------------------------------------- |
| `POST` | `/stocks`                                  | Sets the initial state of the bank. Overwrites existing stocks. | `200 OK` or `400 Bad Request`                                        |
| `GET`  | `/stocks`                                  | Returns the current state of the bank (all available stocks).   | `200 OK`                                                             |
| `POST` | `/wallets/{wallet_id}/stocks/{stock_name}` | Buys or sells a single stock. Body: `{"type": "buy"\|"sell"}`   | `200 OK`, `400` (Insufficient funds/stocks), `404` (Stock not found) |
| `GET`  | `/wallets/{wallet_id}`                     | Returns the current state of a specific wallet.                 | `200 OK`                                                             |
| `GET`  | `/wallets/{wallet_id}/stocks/{stock_name}` | Returns the quantity of a specific stock in a wallet.           | `200 OK`                                                             |
| `GET`  | `/log`                                     | Returns the audit log of all successful wallet transactions.    | `200 OK`                                                             |
| `POST` | `/chaos`                                   | Kills an instance that serves this request.                     | Connection Dropped / `502`                                           |

## Engineering Decisions

**Language Selection: Go vs. Java:**
The repository includes a secondary implementation located in `./legacy-java-server`. It achieves the same requirements using Spring Boot. However, Go was chosen as the primary backend language for production due to critical performance and operational trade-offs:

- **Recovery Time:** In our system, nodes killed via the `/chaos` endpoint must recover almost instantly to prevent queue buildup. Go inherently provides near instant startup times (restarting an instance in **< 1s**).
- **Java's Cold Start:** Even after aggressive optimizations, the Spring Boot Java instance took approximately **7-8s** to restart, which was unacceptable for this specific use case.
- **The GraalVM Experiment:** (Available on `graalvm` branch) We successfully compiled the Java application to a native executable using GraalVM, which solved the startup time issue. However, it was ultimately rejected due to the severe overhead cost during the build phase — specifically, the compilation took far too long and consumed an excessive amount of RAM. Go delivers the necessary performance and tiny footprint out-of-the-box without the extreme build overhead.

### Testing the Legacy Java Spring Boot implementation

If you wish check the differences between the primary Go implementation and the Legacy Java version, follow these steps in `docker-compose.yml`:

1.  **Change the build context**: Replace `./stock-market` with `./legacy-java-server/stock-market` in the `stock-market-app` service.
2.  **Adjust Healthcheck**: Since the Spring Boot application has a significantly longer startup time, increase the `start_period` in the `healthcheck` section to at least `10s` to prevent the container from being marked as unhealthy prematurely.
3.  **Run**: Rebuild and start the cluster using the standard `start.sh` or `start.bat` command.
