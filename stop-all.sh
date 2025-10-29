#!/bin/bash

echo "🛑 Stopping all Barrisense microservices (Git Bash windows)..."

# Kill all mintty windows with those titles
taskkill //FI "WINDOWTITLE eq barrisense-*" //T //F >/dev/null 2>&1

# Stop Gradle daemons
./gradlew --stop >/dev/null 2>&1

echo "✅ All Barrisense microservices stopped (IntelliJ and other terminals left alone)."
