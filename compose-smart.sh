#!/bin/bash
# 🚀 Smart Docker Compose builder/runner for Barrisense
# Supports: ./compose-smart.sh <service> | all

set -e

# 🧩 Ensure the shared Docker network exists
if ! docker network inspect barrisense-net >/dev/null 2>&1; then
  echo "🌐 Creating Docker network: barrisense-net"
  docker network create barrisense-net
else
  echo "✅ Docker network 'barrisense-net' already exists"
fi

# 🧭 Internal ports as defined in each service's application.yml
declare -A SERVICE_PORTS=(
  ["gateway-service"]=8080
  ["auth-service"]=8081
  ["complaint-service"]=8082
  ["user-service"]=8083
)


SERVICES=("gateway-service" "auth-service" "user-service" "complaint-service")
GHCR_NAMESPACE="ghcr.io/barrisense"

DOCKER_COMPOSE_DEV="docker-compose-dev.yml"
DOCKER_COMPOSE_RABBIT="rabbit-compose.yml"
DOCKER_COMPOSE_MYSQL="mysql-compose.yml"

get_version() {
  local service=$1
  local gradle_file="./$service/build.gradle"
  if [[ -f "$gradle_file" ]]; then
    grep "^version" "$gradle_file" | awk -F'"' '{print $2}' | tr -d '\r\n'
  elif [[ -f "./$service/VERSION" ]]; then
    cat "./$service/VERSION" | tr -d '\r\n'
  else
    echo "latest"
  fi
}

build_if_needed() {
  local service=$1
  local version=$2
  local image="${GHCR_NAMESPACE}/${service}:${version}"

#  echo "🧠 Checking for local image: $image"
#
#  if docker image inspect "$image" > /dev/null 2>&1; then
#    echo "✅ Local image found: $image"
#  else
    echo "❌ No local image found — building..."
    ROOT_PATH=$(pwd)
    docker build \
      --build-arg SERVICE_NAME="$service" \
      -t "$image" \
      -f "$ROOT_PATH/$service/Dockerfile" \
      "$ROOT_PATH"

}

run_rabbit() {
  if [[ -f "$DOCKER_COMPOSE_RABBIT" ]]; then
    echo "🐇 Starting RabbitMQ via $DOCKER_COMPOSE_RABBIT..."
    docker compose -f "$DOCKER_COMPOSE_RABBIT" up -d
  else
    echo "⚠️  No $DOCKER_COMPOSE_RABBIT file found. Skipping RabbitMQ."
  fi
}

run_mysql() {
  if [[ -f "$DOCKER_COMPOSE_MYSQL" ]]; then
    echo "🐬 Starting MySQL via $DOCKER_COMPOSE_MYSQL..."
#    docker compose -f "$DOCKER_COMPOSE_MYSQL" up -d
  else
    echo "⚠️  No $DOCKER_COMPOSE_MYSQL file found. Skipping MySQL."
  fi
}

SERVICE_ARG=$1
if [[ -z "$SERVICE_ARG" ]]; then
  echo "Usage: ./compose-smart.sh <service-name> | all"
  exit 1
fi

if [[ "$SERVICE_ARG" == "all" ]]; then
  run_rabbit
  run_mysql

  declare -A VERSIONS
  for SERVICE in "${SERVICES[@]}"; do
    VERSIONS[$SERVICE]=$(get_version "$SERVICE")
    build_if_needed "$SERVICE" "${VERSIONS[$SERVICE]}"
  done

  export GATEWAY_VERSION="${VERSIONS[gateway-service]}"
  export AUTH_VERSION="${VERSIONS[auth-service]}"
  export COMPLAINT_VERSION="${VERSIONS[complaint-service]}"
  export USER_VERSION="${VERSIONS[user-service]}"

  docker compose -f "$DOCKER_COMPOSE_DEV" up -d

  echo "✅ All services and gateway are up!"
else
  VERSION=$(get_version "$SERVICE_ARG")
  build_if_needed "$SERVICE_ARG" "$VERSION"

  # 🧩 Determine internal port
  internal_port=${SERVICE_PORTS[$SERVICE_ARG]:-8080}  # default to 8080 if not found
  echo "🌐 Exposing $SERVICE_ARG (internal port $internal_port) on localhost:8080..."

  docker run -d \
    -p 8080:${internal_port} \
    --name "${SERVICE_ARG}-exposed" \
    --network barrisense-net \
    ghcr.io/barrisense/${SERVICE_ARG}:$VERSION
fi