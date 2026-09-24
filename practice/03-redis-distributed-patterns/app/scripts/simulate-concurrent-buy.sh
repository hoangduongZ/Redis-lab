#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-naive}"   # naive | redis
COUNT="${2:-20}"

if [[ "$MODE" != "naive" && "$MODE" != "redis" ]]; then
  echo "Usage: $0 [naive|redis] [so-luong-request]"
  exit 1
fi

ENDPOINT="${MODE}-ticket/buy"
PORTS=(8081 8082)

echo "Reset ve truoc khi thi nghiem..."
curl -s -X POST "http://localhost:8081/api/ticket/reset" > /dev/null

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

echo "Ban $COUNT request dong thoi vao /api/${ENDPOINT}, chia deu cho cong ${PORTS[*]}..."

for ((i = 1; i <= COUNT; i++)); do
  PORT=${PORTS[$(( (i - 1) % ${#PORTS[@]} ))]}
  (
    curl -s -X POST "http://localhost:${PORT}/api/${ENDPOINT}?userId=user-${i}" > "$TMP_DIR/res-${i}.json"
  ) &
done
wait

SUCCESS_COUNT=$(grep -l "Thanh cong" "$TMP_DIR"/res-*.json 2>/dev/null | wc -l | tr -d ' ')

echo ""
echo "=== Ket qua (${MODE}-ticket) ==="
echo "${SUCCESS_COUNT} / ${COUNT} request bao 'Thanh cong'."
if [[ "$SUCCESS_COUNT" -gt 1 ]]; then
  echo "=> BAN LO VE (dung y neu dang test naive, la loi neu dang test redis)."
else
  echo "=> Dung 1 ve duy nhat duoc ban."
fi
