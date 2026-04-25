#!/bin/bash
if [ -z "$1" ]; then
    echo "Usage: ./start.sh <PORT>"
    exit 1
fi

export APP_PORT=$1
docker-compose up --build -d --wait

echo "Server started on http://localhost:$APP_PORT"
echo "API cluster is running with 3 instances."