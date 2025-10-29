#!/bin/bash

echo "🚀 Starting all Barrisense microservices (in new Git Bash windows)..."

declare -A services=(
  ["gateway-service"]=8080
  ["user-service"]=8083
  ["complaint-service"]=8082
  ["auth-service"]=8081
)

# Path to Git Bash executable (adjust if needed)
GIT_BASH_EXE="/c/Program Files/Git/git-bash.exe"
ROOT_PATH=$(pwd)

# ✅ Check if Git Bash exists before continuing
if [ ! -f "$GIT_BASH_EXE" ]; then
  echo "❌ Could not find Git Bash at: $GIT_BASH_EXE"
  echo "Please check your installation path and update GIT_BASH_EXE in this script."
  exit 1
fi

for service in "${!services[@]}"; do
  port=${services[$service]}
  echo "▶ Opening Git Bash for $service on port $port..."

  # ⚙️ Empty first arg → correct syntax for Windows `start`
  # Then inside Git Bash: set ANSI title, cd, run Gradle, keep shell open
  start "" "$GIT_BASH_EXE" -c "echo -ne '\033]0;barrisense-${service}\007' && cd \"$ROOT_PATH\" && ./gradlew :${service}:bootRun; exec bash"
done

echo "✅ All services started in new Git Bash windows!"
