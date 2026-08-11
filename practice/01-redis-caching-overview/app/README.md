# Lab 1 — Caching Overview

Khung code thực hành cho [`giao-an/01-redis-caching-overview.md`](../../../giao-an/01-redis-caching-overview.md). Chi tiết kế hoạch: [`../PLAN.md`](../PLAN.md).

## Chạy hạ tầng

```bash
docker compose up -d
```

- Postgres: `localhost:5432` (db/user/pass: `lab01`)
- Redis: `localhost:6379`
- Redis Commander (xem trực quan bảng trắng): http://localhost:8081

## Chạy app

```bash
./mvnw spring-boot:run
```

App tự seed vài cuốn sách khi khởi động (có "Harry Potter").

## Thử nghiệm 3 giai đoạn

```bash
# Buoc 1: khong cache -> luon ~2000ms
curl http://localhost:8080/api/no-cache/books/Harry%20Potter

# Buoc 2: cache thu cong bang HashMap -> lan 2 tro di ~0ms (nhung chi dung tren 1 instance)
curl http://localhost:8080/api/manual-cache/books/Harry%20Potter
curl http://localhost:8080/api/manual-cache/books/Harry%20Potter

# Buoc 3: Redis @Cacheable -> lan 2 tro di ~0ms, dung chung cho moi instance
curl http://localhost:8080/api/redis-cache/books/Harry%20Potter
curl http://localhost:8080/api/redis-cache/books/Harry%20Potter
```

Mỗi response trả kèm `elapsedMs` để so sánh bằng số. Sau khi gọi `redis-cache`, mở Redis Commander lên sẽ thấy key `books::Harry Potter`.

---
Đánh giá cá nhân:
Manual cache hiện tại thấy tốc độ trả ra nhanh hơn, mất 0ms
Redis cache mất 2ms