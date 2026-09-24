#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-naive}"          # naive | redis
IP="${2:-192.168.1.1}"
COUNT="${3:-20}"

if [[ "$MODE" != "naive" && "$MODE" != "redis" ]]; then
  echo "Usage: $0 [naive|redis] [ip] [so-luong-request]"
  exit 1
fi

PORTS=(8081 8082)
TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

echo "Ban $COUNT request dong thoi vao /api/login/${MODE}?ip=${IP}, chia deu cho cong ${PORTS[*]} (nguong 5 req/s)..."

for ((i = 1; i <= COUNT; i++)); do
  PORT=${PORTS[$(( (i - 1) % ${#PORTS[@]} ))]}
  (
    CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "http://localhost:${PORT}/api/login/${MODE}?ip=${IP}")
    echo "$CODE" >> "$TMP_DIR/codes.txt"
  ) &
done
wait

TOTAL_OK=$(grep -c "^200$" "$TMP_DIR/codes.txt" 2>/dev/null || true)
TOTAL_BLOCKED=$(grep -c "^429$" "$TMP_DIR/codes.txt" 2>/dev/null || true)

echo ""
echo "=== Ket qua (${MODE}, nguong 5 req/s) ==="
echo "${TOTAL_OK} / ${COUNT} request duoc cho qua (HTTP 200)."
echo "${TOTAL_BLOCKED} / ${COUNT} request bi chan (HTTP 429)."
