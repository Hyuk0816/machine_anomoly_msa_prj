#!/bin/bash
# Ollama initialization script

set -e

MODEL_NAME="${OLLAMA_MODEL:-gpt-oss:20b}"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "[Ollama Init] Starting Ollama server..."
ollama serve &
OLLAMA_PID=$!

echo "[Ollama Init] Waiting for Ollama server to be ready..."
for i in $(seq 1 $MAX_RETRIES); do
    if ollama list > /dev/null 2>&1; then
        echo "[Ollama Init] Ollama server is ready"
        break
    fi
    if [ $i -eq $MAX_RETRIES ]; then
        echo "[Ollama Init] ERROR: Ollama server failed to start after $MAX_RETRIES attempts"
        exit 1
    fi
    echo "[Ollama Init] Waiting for Ollama server... (attempt $i/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

echo "[Ollama Init] Checking if model '$MODEL_NAME' exists..."
MODEL_BASE=$(echo $MODEL_NAME | cut -d: -f1)

if ollama list 2>/dev/null | grep -q "$MODEL_BASE"; then
    echo "[Ollama Init] Model '$MODEL_NAME' already exists in volume, skipping download"
else
    echo "[Ollama Init] Model not found in volume, pulling '$MODEL_NAME'..."
    echo "[Ollama Init] This may take a while depending on your network speed (approx. 14GB)"
    ollama pull $MODEL_NAME
    echo "[Ollama Init] Model '$MODEL_NAME' downloaded successfully"
fi

echo "[Ollama Init] Initialization complete"
echo "[Ollama Init] Model: $MODEL_NAME"
echo "[Ollama Init] API endpoint: http://localhost:11434"

wait $OLLAMA_PID
