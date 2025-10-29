#!/bin/bash

echo "🛑 Stopping all Barrisense microservices (Git Bash windows)..."

# mintty titles show up in the window list, so we can target them
taskkill //FI "WINDOWTITLE eq barrisense-*" //T //F >nul 2>&1

# Stop Gradle daemons just in case
./gradlew --stop >nul 2>&1

echo "✅ All Barrisense microservices stopped (IntelliJ and other terminals left alone)."
