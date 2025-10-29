#!/bin/bash

echo "🛑 Stopping all Barrisense microservices..."

if [ ! -d "logs" ]; then
  echo "No logs directory found. Nothing to stop."
  exit 0
fi

for pidfile in logs/*.pid; do
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    service=$(basename "$pidfile" .pid)
    if ps -p "$pid" > /dev/null 2>&1; then
      echo "⛔ Stopping $service (PID $pid)..."
      kill "$pid"
    else
      echo "⚠️  Process $pid for $service not found."
    fi
    rm -f "$pidfile"
  fi
done

echo "✅ All services stopped."
