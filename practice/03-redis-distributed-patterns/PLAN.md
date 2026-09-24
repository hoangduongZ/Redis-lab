# Kế Hoạch Practice — Bài 3: Siêu Năng Lực Phân Tán (Lock & Rate Limit)

> Bám sát [`giao-an/03-redis-distributed-patterns.md`](../../giao-an/03-redis-distributed-patterns.md). Trọng tâm: **Distributed Lock** (`SETNX`/`setIfAbsent`) và **Rate Limiting** (`INCR`). Không đụng Persistence/RDB/AOF/Cluster — để dành bài 4.

## 1. Mục tiêu practice

Bài này **bắt buộc phải chạy từ 2 server trở lên**, nếu không sẽ không bao giờ thấy được "nỗi đau" mà giáo án mô tả — đây là điểm khác biệt lớn nhất so với bài 1, 2 (chỉ cần 1 instance là đủ):
1. Tự tay gây ra thảm họa "bán lố vé": dùng `synchronized` trong Java, chạy 2 instance, bắn nhiều request mua vé đồng thời vào cả 2 → thấy **nhiều hơn 1 người** mua được vé (đúng như giáo án cảnh báo).
2. Sửa bằng Redis Distributed Lock (`setIfAbsent`) → dù chạy bao nhiêu instance, **chỉ đúng 1 người** mua được.
3. Tự tay thử rate limiting bằng `INCR`: gọi API đăng nhập dồn dập từ "một IP", thấy nó bị chặn sau ngưỡng N lần/giây.

## 2. Tech stack

| Thành phần | Lựa chọn | Vì sao |
|---|---|---|
| Backend | Java 17 + Spring Boot 3 | nối tiếp bài 1, 2 |
| Redis client | `StringRedisTemplate` | đúng như code mẫu trong giáo án (`setIfAbsent`, `INCR`/`increment`) |
| Chạy đa instance | Docker Compose: build 1 image, chạy 2 container (`app-1` cổng 8081, `app-2` cổng 8082) cùng trỏ vào 1 Redis | mô phỏng "10 nhân viên, 10 quầy" thật sự, không giả vờ bằng code |
| Redis | 1 container dùng chung cho cả 2 app instance | chính là "Người Phân Xử" đứng giữa |
| Đo lường race condition | script bash bắn N request đồng thời (background `&` + `wait`) chia đều cho cổng 8081/8082 | đủ để tạo race thật, không cần công cụ load-test nặng (k6/JMeter) cho bài này |

## 3. Cấu trúc project dự kiến

```
practice/03-redis-distributed-patterns/
├── PLAN.md
└── app/
    ├── docker-compose.yml        # redis + app-1 (8081) + app-2 (8082), cùng build từ Dockerfile
    ├── Dockerfile                # multi-stage: build bằng mvnw, chạy bằng JRE — cần thiết vì bài này chạy app trong container, khác 01/02 chạy host
    ├── pom.xml
    ├── README.md
    ├── src/main/java/com/redislab/lab03/
    │   ├── Lab03Application.java
    │   ├── ticket/
    │   │   ├── NaiveTicketService.java     # `synchronized` + boolean cờ trong RAM — nỗi đau
    │   │   ├── RedisLockTicketService.java # setIfAbsent(lockKey, userId, Duration) — thuốc giải, đúng code giáo án
    │   │   └── TicketController.java       # /api/naive-ticket/buy, /api/redis-ticket/buy, + /reset
    │   └── ratelimit/
    │       ├── NaiveRateLimiterService.java # AtomicInteger đếm theo giây, riêng từng instance — để lộ điểm yếu tương tự bài toán trước (10 server = 10 bộ đếm riêng)
    │       ├── RedisRateLimiterService.java # INCR + EXPIRE(1s) trên cùng 1 key theo IP — đúng cơ chế giáo án mô tả
    │       └── LoginController.java        # /api/login/naive|redis?ip=... , trả 429 khi vượt ngưỡng
    └── scripts/
        ├── simulate-concurrent-buy.sh   # bắn N request mua vé song song, chia đều cho 2 cổng, đếm số vé "bán thành công"
        └── simulate-login-flood.sh      # bắn N request login song song theo 1 IP, chia đều cho 2 cổng, đếm số request lọt qua/bị chặn
```

Không thêm `config/RedisConfig.java` như dự kiến ban đầu: `StringRedisTemplate` đã được Spring Boot tự động cấu hình sẵn khi có `spring-boot-starter-data-redis`, không cần khai báo bean thủ công cho `setIfAbsent`/`INCR` thô.

### Endpoint dự kiến

**Distributed Lock:**
- `POST /api/naive-ticket/buy?userId=` — dùng `synchronized`, chạy trên từng instance riêng
- `POST /api/redis-ticket/buy?userId=` — dùng Redis lock dùng chung
- `POST /api/ticket/reset` — reset lại vé để chạy thí nghiệm nhiều lần

**Rate Limiting:**
- `POST /api/login/naive?ip=` — bộ đếm riêng từng instance (điểm yếu tương tự lock)
- `POST /api/login/redis?ip=` — bộ đếm dùng chung qua Redis `INCR`, chặn đúng ngưỡng N lần/giây kể cả khi request rải đều 2 instance

## 4. Các bước triển khai

1. **Scaffold**: project riêng cho bài 3, `docker-compose.yml` build 1 image chạy 2 lần (2 container, khác `SERVER_PORT`), cùng trỏ `spring.redis.host` về 1 container Redis.
2. **Gây thảm họa trước**: `NaiveTicketService` — 1 vé, dùng `synchronized` + biến `boolean sold` trong RAM. Chạy `simulate-concurrent-buy.sh` bắn 20 request đồng thời chia đều cho 8081/8082 → đếm số lần "Thành công" → kỳ vọng ra **nhiều hơn 1**.
3. **Thuốc giải**: `RedisLockTicketService` y hệt code mẫu giáo án (`setIfAbsent` + TTL 10s + `finally` xoá lock). Chạy lại script tương tự → kỳ vọng **đúng 1** lần thành công, bất kể chia bao nhiêu request cho 2 cổng.
4. **Rate limiting — gây đau trước rồi mới giải**: `NaiveRateLimiterService` đếm bằng `AtomicInteger` riêng từng instance → bắn request xen kẽ 2 cổng, nhận ra tổng số lọt qua **gấp đôi** ngưỡng mong muốn (vì mỗi instance đếm riêng). `RedisRateLimiterService` dùng `INCR` + `EXPIRE(1s)` trên Redis dùng chung → tổng số lọt qua đúng bằng ngưỡng N, dù rải đều 2 instance.
5. **Đo bằng số**: script in ra "X/N request thành công" cho mỗi kịch bản, so sánh naive vs Redis rõ ràng bằng con số, không chỉ bằng cảm giác.

## 5. Tiêu chí hoàn thành

- [ ] Chạy được 2 instance (`app-1`, `app-2`) cùng lúc qua `docker-compose up --scale` hoặc 2 service riêng trong compose.
- [ ] Chứng minh được naive lock (`synchronized`) bán lố vé khi có ≥ 2 instance.
- [ ] Chứng minh được Redis lock luôn chỉ bán đúng 1 vé, bất kể số instance.
- [ ] Chứng minh được naive rate limiter cho lọt quá ngưỡng khi rải request qua nhiều instance.
- [ ] Chứng minh được Redis rate limiter giữ đúng ngưỡng dù nhiều instance.
- [ ] Không dùng Redisson hay thư viện lock nào khác — chỉ dùng đúng `setIfAbsent`/`INCR` thô như giáo án dạy, để hiểu bản chất trước khi dùng thư viện có sẵn sau này.

## 6. Việc KHÔNG làm ở bài này

- Không RDB/AOF, không bàn chuyện mất dữ liệu khi cúp điện (để dành bài 4).
- Không Redis Cluster/Sentinel/replication.
- Không dùng thư viện lock nâng cao (Redisson, Lua script `SET NX PX` phức tạp) — giữ đúng mức "nguyên thuỷ" mà giáo án minh hoạ.
