# Lab 3 — Siêu Năng Lực Phân Tán (Lock & Rate Limit)

Khung code thực hành cho [`giao-an/03-redis-distributed-patterns.md`](../../../giao-an/03-redis-distributed-patterns.md). Chi tiết kế hoạch: [`../PLAN.md`](../PLAN.md).

Khác với bài 1, 2: bài này **bắt buộc chạy từ 2 instance trở lên** để thấy được nỗi đau, nên cả app lẫn Redis đều chạy trong Docker Compose (không chạy `mvnw spring-boot:run` trên host như trước).

## Cấu trúc mã nguồn

```
Lab03Application.java

ticket/                        # Bài toán: 1000 người tranh nhau đúng 1 vé
├─ TicketController.java       #   REST: /naive-ticket, /redis-ticket, /ticket/reset
├─ NaiveTicketService.java     #   "Nỗi đau": synchronized chỉ khoá được trong 1 JVM
└─ RedisLockTicketService.java #   Thuốc giải: setIfAbsent làm chìa khoá dùng chung

ratelimit/                     # Bài toán: chặn hacker dò login quá N lần/giây
├─ LoginController.java        #   REST: /login/naive, /login/redis
├─ NaiveRateLimiterService.java #  "Nỗi đau": AtomicInteger đếm riêng từng instance
└─ RedisRateLimiterService.java # Thuốc giải: INCR + EXPIRE dùng chung qua Redis
```

## Chạy 2 instance + Redis

```bash
docker compose up --build
```

- `app-1`: http://localhost:8081
- `app-2`: http://localhost:8082
- Redis: `localhost:6379` (dùng chung cho cả 2 app)

## Thí nghiệm 1: Bán lố vé (Distributed Lock)

```bash
# Naive: synchronized chỉ khoá được trên từng instance -> kỳ vọng > 1 người "Thanh cong"
./scripts/simulate-concurrent-buy.sh naive 20

# Redis lock: setIfAbsent dùng chung -> kỳ vọng đúng 1 người "Thanh cong"
./scripts/simulate-concurrent-buy.sh redis 20
```

Gọi tay từng endpoint nếu muốn xem chi tiết:

```bash
curl -X POST "http://localhost:8081/api/naive-ticket/buy?userId=user-A"
curl -X POST "http://localhost:8082/api/naive-ticket/buy?userId=user-B"

curl -X POST "http://localhost:8081/api/ticket/reset"
```

## Thí nghiệm 2: Rate Limiting

```bash
# Naive: bộ đếm riêng từng instance -> kỳ vọng số request lọt qua > nguong 5/s (co the toi ~10)
./scripts/simulate-login-flood.sh naive 192.168.1.1 20

# Redis: INCR dùng chung -> kỳ vọng đúng 5 request lọt qua, dù rải đều 2 instance
./scripts/simulate-login-flood.sh redis 192.168.1.1 20
```

## Ghi chú

- Không dùng Redisson hay thư viện lock nào khác — chỉ `setIfAbsent`/`INCR` thô đúng như giáo án, để hiểu bản chất trước khi dùng thư viện có sẵn sau này.
- Response của mỗi endpoint có kèm cổng instance đã xử lý (`:8081`/`:8082`) để nhìn thấy rõ request nào rơi vào instance nào.
