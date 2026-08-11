# Kế Hoạch Practice — Bài 2: Hình Dáng Của Dữ Liệu (Sorted Set & bạn bè)

> Bám sát [`giao-an/02-redis-data-structures.md`](../../giao-an/02-redis-data-structures.md). Trọng tâm là **Sorted Set** (bảng xếp hạng); List/Set/Hash chỉ "lướt nhanh" đúng như giáo án, không đào sâu. Không đụng Distributed Lock — để dành bài 3.

## 1. Mục tiêu practice

Tự tay cảm nhận **vì sao "sắp xếp" là việc đắt đỏ**, và vì sao ZSet giải quyết nó bằng cách "luôn ở trạng thái đã sắp xếp sẵn":
1. Lưu bảng xếp hạng trong Postgres, gọi `ORDER BY score DESC LIMIT 10` trên bảng có nhiều dòng → đo thời gian, thấy nó chậm dần khi dữ liệu phình to.
2. Lưu cùng bảng xếp hạng bằng Redis ZSet, gọi Top 10 → luôn nhanh bất kể có bao nhiêu người chơi.
3. Nếm thử nhanh 3 "hình dáng" còn lại (List, Set, Hash) qua 3 tình huống nhỏ mà giáo án gợi ý: hàng đợi email, đếm visitor duy nhất, hồ sơ người chơi.

## 2. Tech stack

| Thành phần | Lựa chọn | Vì sao |
|---|---|---|
| Backend | Java 17 + Spring Boot 3 | nối tiếp bài 1 |
| Baseline "nỗi đau" | PostgreSQL, bảng `player_score` **không đánh index cột `score`** | để phép `ORDER BY` phải quét + sort thật sự, đúng tinh thần giáo án |
| Công cụ Redis | `RedisTemplate` (KHÔNG dùng `@Cacheable` nữa) | đúng như giáo án chuyển sang thao tác trực tiếp bằng ZSetOperations |
| Data structures | ZSet (chính), List, Set, Hash (phụ) | đúng mục 5 của giáo án |
| Hạ tầng | Docker Compose (postgres, redis, redis-commander) | Redis Commander cho thấy trực quan "tấm bảng gỗ có khe trượt" — mở lên là thấy ZSet đã sắp xếp sẵn |
| Seed dữ liệu | script sinh N người chơi + điểm ngẫu nhiên (N cấu hình được, mặc định 500,000) | phải đủ nhiều dòng thì Postgres mới "bốc khói" thật, không diễn |

## 3. Cấu trúc project dự kiến

```
practice/02-redis-data-structures/
├── PLAN.md
└── app/
    ├── docker-compose.yml
    ├── pom.xml
    └── src/main/java/com/redislab/lab02/
        ├── Lab02Application.java
        ├── leaderboard/
        │   ├── PlayerScore.java            # JPA entity: id, playerName, score (KHÔNG index score)
        │   ├── PlayerScoreRepository.java
        │   ├── LeaderboardSeeder.java       # sinh N bản ghi điểm ngẫu nhiên khi khởi động (config qua application.yml)
        │   ├── PostgresLeaderboardService.java  # ORDER BY score DESC LIMIT 10 — nỗi đau
        │   ├── RedisLeaderboardService.java     # ZSetOperations: add, reverseRangeWithScores, reverseRank, score
        │   └── LeaderboardController.java
        └── playground/                      # 3 hình dáng còn lại, mỗi cái 1 use-case nhỏ, không lan man
            ├── EmailQueueService.java        # List: LPUSH job / RPOP job xử lý dần
            ├── UniqueVisitorService.java      # Set: SADD userId theo ngày, SCARD đếm duy nhất
            ├── PlayerProfileService.java      # Hash: HSET/HGET từng field (tên, tuổi) không cần cả JSON
            └── PlaygroundController.java
```

### Endpoint dự kiến

**Trọng tâm — so sánh trực diện:**
- `GET /api/postgres-leaderboard/top10` — full scan + sort trên Postgres, trả kèm thời gian đo được (ms)
- `POST /api/redis-leaderboard/scores?player=&score=` — tương đương `ZADD`
- `GET /api/redis-leaderboard/top10` — `ZREVRANGE ... WITHSCORES`, trả kèm thời gian đo được (ms)
- `GET /api/redis-leaderboard/rank/{player}` — bonus nhỏ: `ZREVRANK` + `ZSCORE`, cho thấy ZSet còn trả lời được cả "tôi đang xếp hạng thứ mấy" mà không cần quét gì cả

**Playground (mỗi cái 2 endpoint, đủ để sờ được, không đào sâu):**
- `POST /api/queue/email` (LPUSH), `GET /api/queue/email/next` (RPOP)
- `POST /api/visitors/{day}?userId=` (SADD), `GET /api/visitors/{day}/count` (SCARD)
- `POST /api/profiles/{playerId}` (HSET nhiều field), `GET /api/profiles/{playerId}` (HGETALL)

## 4. Các bước triển khai

1. **Scaffold**: thêm dependency Redis vào `pom.xml` mới (project riêng cho bài 2, độc lập với bài 1), `docker-compose.yml` (postgres + redis + redis-commander).
2. **Nỗi đau trước**: entity `PlayerScore` **cố tình không index `score`**, seeder sinh 500,000 dòng ngẫu nhiên, `PostgresLeaderboardService.getTop10()` chạy `ORDER BY score DESC LIMIT 10` và log thời gian thực thi.
3. **Thuốc giải**: `RedisLeaderboardService` dùng `RedisTemplate.opsForZSet()` — `add`, `reverseRangeWithScores(0,9)`, `reverseRank`. Seed cùng 500,000 người chơi đó vào ZSet để so sánh công bằng (cùng dữ liệu, khác cấu trúc lưu trữ).
4. **Đo và so sánh**: gọi cả hai endpoint Top 10 nhiều lần, ghi lại độ trễ — chứng minh bằng số, không chỉ bằng lời.
5. **Playground 3 hình dáng còn lại**: viết nhanh List/Set/Hash service, mỗi cái chỉ cần đủ để "sờ" được cảm giác, không thêm business logic thừa.
6. **Mở Redis Commander**: tự tay nhìn `game_leaderboard` là một ZSet đã sắp xếp sẵn, nhìn `email_queue` là List, nhìn `visitors:2026-08-11` là Set, nhìn `profile:playerId` là Hash — biến khái niệm trừu tượng thành thứ nhìn thấy được.

## 5. Tiêu chí hoàn thành

- [ ] Seed được ≥ 500,000 dòng vào Postgres và ZSet với cùng dữ liệu.
- [ ] `postgres-leaderboard/top10` đo được độ trễ rõ rệt (so sánh trước/sau khi tăng N).
- [ ] `redis-leaderboard/top10` luôn nhanh, không phụ thuộc vào N.
- [ ] `redis-leaderboard/rank/{player}` trả lời được thứ hạng mà không cần quét toàn bộ.
- [ ] 3 endpoint playground (List/Set/Hash) chạy được, mỗi cái mở Redis Commander lên nhìn thấy đúng hình dáng dữ liệu tương ứng.
- [ ] Không có Distributed Lock, không xử lý tình huống "bán vé đồng thời" (để dành bài 3).

## 6. Việc KHÔNG làm ở bài này

- Không giải bài toán over-selling / race condition (bài 3: Distributed Lock).
- Không tối ưu Postgres bằng index cho `score` — mất đi mục đích "gây đau" của bài học.
- Không dùng lại `@Cacheable` — giáo án đã chuyển hẳn sang thao tác Redis trực tiếp qua `RedisTemplate`.
