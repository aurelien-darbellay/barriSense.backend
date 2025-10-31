#!/bin/bash

echo "🚀 Starting Barrisense ecosystem..."

# ----------------------------
# 1️⃣  SETTINGS
# ----------------------------

declare -A services=(
  ["gateway-service"]=8080
  ["user-service"]=8083
  ["complaint-service"]=8082
  ["auth-service"]=8081
)

GIT_BASH_EXE="/c/Program Files/Git/git-bash.exe"
ROOT_PATH=$(pwd)

DOCKER_COMPOSE_FILE="$ROOT_PATH/rabbit-compose.yml"

# ----------------------------
# 2️⃣  CHECK GIT BASH PATH
# ----------------------------

if [ ! -f "$GIT_BASH_EXE" ]; then
  echo "❌ Could not find Git Bash at: $GIT_BASH_EXE"
  echo "Please update GIT_BASH_EXE in this script."
  exit 1
fi

# ----------------------------
# 3️⃣  CHECK DOCKER INSTALLATION
# ----------------------------

if ! command -v docker &> /dev/null; then
  echo "❌ Docker not found! Please install Docker Desktop before running this script."
  exit 1
fi

# ----------------------------
# 4️⃣  ENSURE DOCKER IS RUNNING
# ----------------------------

echo "🧩 Checking Docker daemon status..."

if ! docker info > /dev/null 2>&1; then
  echo "⚠️  Docker is not running. Attempting to start Docker Desktop..."
  powershell.exe -Command "Start-Process 'C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe'"

  echo "⏳ Waiting for Docker to start..."
  for i in {1..30}; do
    sleep 2
    if docker info > /dev/null 2>&1; then
      echo "✅ Docker is now running!"
      break
    fi
    echo "   ...still waiting ($i/30)"
  done

  # If still not running
  if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker did not start in time. Please start Docker Desktop manually and retry."
    exit 1
  fi
else
  echo "✅ Docker daemon is running."
fi

# ----------------------------
# 5️⃣  RUN DOCKER COMPOSE
# ----------------------------

if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
  echo "⚠️  No docker-compose.yml found in $ROOT_PATH — skipping Docker Compose step."
else
  echo "🐳 Starting Docker Compose stack (RabbitMQ, etc.)..."
  docker compose -f "$DOCKER_COMPOSE_FILE" up -d
  if [ $? -ne 0 ]; then
    echo "❌ docker compose up failed. Aborting startup."
    exit 1
  fi
  echo "✅ Docker Compose stack is up!"
fi

# ----------------------------
# 6️⃣  START SPRING SERVICES
# ----------------------------

echo "🚀 Starting Spring Boot microservices (each in a new Git Bash window)..."

for service in "${!services[@]}"; do
  port=${services[$service]}
  echo "▶ Opening Git Bash for $service on port $port..."
  start "" "$GIT_BASH_EXE" -c "echo -ne '\033]0;barrisense-${service}\007' && cd \"$ROOT_PATH\" && ./gradlew :${service}:bootRun; exec bash"
done

echo "✅ All Barrisense services launched successfully!"
