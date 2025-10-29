#!/bin/bash
echo "🚀 Starting all Barrisense microservices..."

declare -A services=(
  ["gateway-service"]=8080
  ["user-service"]=8083
  ["complaint-service"]=8082
  ["auth-service"]=8081
)

for s in "${!services[@]}"; do
  port=${services[$s]}
  echo "▶ Starting $s on port $port..."
  (cd $s && ./gradlew bootRun > "../logs/$s.log" 2>&1 &)
done

echo "✅ All services started in background. Logs in /logs/"
