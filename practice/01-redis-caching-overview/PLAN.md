# Kế Hoạch Practice — Bài 1: Từ Nỗi Đau Đến Bảng Trắng (Caching)

> Bám sát [`giao-an/01-redis-caching-overview.md`](../../giao-an/01-redis-caching-overview.md). Không đụng tới Sorted Set / Leaderboard / scaling — những thứ đó để dành cho bài 2, 3, 4.

## 1. Mục tiêu practice

Không phải "code cho xong", mà là **tự tay cảm nhận được 3 con số khác nhau**:
1. Gọi API lần đầu → chờ ~2 giây (chui xuống hầm Postgres).
2. Gọi lại (cache thủ công bằng `HashMap`) → tức thời, nhưng **chỉ đúng trên 1 instance**.
3. Gọi lại (Redis `@Cacheable`) → tức thời, và **đúng trên mọi instance** (chứng minh luôn luận điểm "10 server cần 1 bảng trắng chung" mà giáo án nói ở Bước 3).

## 2. Tech stack (tự chọn theo nhu cầu bài học, không ép công nghệ thừa)

| Thành phần | Lựa chọn | Vì sao |
|---|---|---|
| Backend | Java 17 + Spring Boot 3 | đúng như code minh họa trong giáo án |
| "Tầng hầm" (DB thật) | PostgreSQL | thay `Thread.sleep` giả bằng độ trễ có lý do thật (query + delay), gần thực tế hơn |
| ORM | Spring Data JPA | chuẩn, không cần học thêm gì lạ |
| "Bảng trắng dùng chung" | Redis | trọng tâm bài học |
| Cache trung gian | Spring Cache abstraction (`@Cacheable`) | đúng những gì giáo án dạy, không tự chế cơ chế riêng |
| Hạ tầng | Docker Compose (postgres, redis, redis-commander) | dựng nhanh, và `redis-commander` giúp *nhìn thấy* bảng trắng bằng mắt — rất Feynman |
| Đo đạc "nỗi đau" | 1 script bash/PowerShell nhỏ gọi API N lần liên tiếp bằng `curl`, in ra tổng thời gian | không cần công cụ load-test nặng nề (k6/JMeter) cho bài vỡ lòng này |

## 3. Cấu trúc project dự kiến

```
practice/01-redis-caching-overview/
├── PLAN.md                      (file này)
└── app/
    ├── docker-compose.yml        # postgres + redis + redis-commander
    ├── pom.xml
    └── src/main/java/com/redislab/lab01/
        ├── Lab01Application.java
        ├── book/
        │   ├── Book.java                 # entity: id, title, content
        │   ├── BookRepository.java       # Spring Data JPA
        │   ├── BookSeeder.java           # CommandLineRunner: seed vài cuốn, có "Harry Potter"
        │   ├── NoCacheBookService.java    # Bước 1: luôn query DB + delay giả lập ổ cứng chậm
        │   ├── ManualCacheBookService.java # Bước 2: tự tay if-else với ConcurrentHashMap
        │   ├── RedisCacheBookService.java  # Bước 3: @Cacheable(value="books", key="#title")
        │   └── BookController.java       # 3 endpoint song song để so sánh trực tiếp
        └── config/
            └── RedisCacheConfig.java     # TTL, serializer (giữ tối giản, không phức tạp hoá)
```

3 endpoint tách riêng (không giấu đi) để bạn gọi trực tiếp và tự so sánh:
- `GET /api/no-cache/books/{title}`
- `GET /api/manual-cache/books/{title}`
- `GET /api/redis-cache/books/{title}`

## 4. Các bước triển khai (làm tới đâu, thầy hướng dẫn tới đó)

1. **Scaffold**: `pom.xml` (Spring Boot Web, Data JPA, Cache, Redis, Postgres driver), `docker-compose.yml`.
2. **Bước 1 — Trải nghiệm nỗi đau**: entity + repository + seed data, `NoCacheBookService` luôn truy vấn Postgres và có delay mô phỏng. Gọi vài lần để thấy nó *luôn* chậm.
3. **Bước 2 — Bảng trắng thủ công**: `ManualCacheBookService` dùng `ConcurrentHashMap` giống hệt logic if-else trong giáo án. Gọi 2 lần: lần 1 chậm, lần 2 nhanh.
4. **Bước 3 — Redis + `@Cacheable`**: cấu hình `RedisCacheConfig`, viết `RedisCacheBookService`. So sánh code: chỉ 1 dòng annotation thay cho cả khối if-else.
5. **Chứng minh luận điểm "cần bảng chung"**: scale `docker-compose` app lên 2 instance (2 port khác nhau). Gọi `manual-cache` xen kẽ giữa 2 instance → thấy nó *vẫn chậm* vì mỗi instance có `HashMap` riêng. Gọi `redis-cache` xen kẽ → luôn nhanh vì dùng chung Redis.
6. **Đo bằng số**: script nhỏ gọi mỗi endpoint N lần, in tổng thời gian ra — biến "cảm giác" thành "bằng chứng".

## 5. Tiêu chí hoàn thành (Definition of Done)

- [x] `docker-compose up` chạy được Postgres + Redis + Redis Commander.
- [x] Gọi `no-cache` nhiều lần → luôn ~2s.
- [x] Gọi `manual-cache` lần 2 → ~0s, nhưng khi có 2 instance thì không nhất quán.
- [x] Gọi `redis-cache` lần 2 → ~0s, nhất quán dù bao nhiêu instance.
- [x] Mở Redis Commander, tự tay thấy key `books::Harry Potter` nằm trong Redis — "nhìn thấy" bảng trắng, không chỉ tin bằng lời.
- [x] Không có bất kỳ dòng code nào đụng tới Sorted Set, phân tán, hay scaling nâng cao (để dành bài sau).

## 6. Việc KHÔNG làm ở bài này (giữ đúng phạm vi giáo án 01)

- Không invalidation/eviction phức tạp, không TTL tinh vi.
- Không Sorted Set / Leaderboard (đó là bài 2 — "Hình Dáng Của Dữ Liệu").
- Không cluster/replication Redis (đó là bài 3–4).
