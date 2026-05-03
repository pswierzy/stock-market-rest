FROM golang:1.26-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o main ./main.go

FROM alpine:latest
RUN apk add --no-cache curl

COPY --from=builder /app/main /main

EXPOSE 8080

CMD ["/main"]