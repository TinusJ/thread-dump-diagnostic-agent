#!/bin/bash

# Thread Dump Diagnostic Agent - Docker Build Script
set -e

echo "🏗️  Building Thread Dump Diagnostic Agent Docker image..."

# Build the JAR first
echo "📦 Building JAR..."
mvn clean package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t thread-dump-diagnostic-agent:latest .

echo "✅ Docker image built successfully!"
echo "🚀 Start with: docker compose up -d"
echo "🔍 Check health: curl http://localhost:8080/api/actuator/health"