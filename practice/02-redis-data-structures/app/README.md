# Lab 2 — Hình Dáng Của Dữ Liệu

Khung code thực hành cho [`giao-an/02-redis-data-structures.md`](../../../giao-an/02-redis-data-structures.md). Chi tiết kế hoạch: [`../PLAN.md`](../PLAN.md).

## Cấu trúc mã nguồn

Mỗi hình dáng dữ liệu Redis được tách thành một lớp `Service` riêng, dễ đọc và
dễ so sánh với giao án. Code chính nằm trong `src/main/java/com/redislab/lab02/`:

```
Lab02Application.java              # Điểm khởi động Spring Boot

leaderboard/                       # ZSet vs Postgres — ai lấy Top 10 nhanh hơn?
├─ LeaderboardController.java      #   REST: /postgres-leaderboard, /redis-leaderboard
├─ PostgresLeaderboardService.java #   "Nỗi đau": quét + sort 500k dòng mỗi lần Top 10
├─ RedisLeaderboardService.java    #   ZSet luôn sắp xếp sẵn (ZADD / ZREVRANGE / ZREVRANK)
├─ PlayerScoreRepository.java      #   Câu SQL ORDER BY score DESC trên bảng KHÔNG index
├─ PlayerScore.java                #   Entity JPA cho bảng player_score (Postgres)
├─ LeaderboardEntry.java           #   Dạng JSON trả về chung cho cả hai phía
└─ LeaderboardSeeder.java          #   Seed cùng N người chơi vào cả Postgres lẫn Redis

playground/                        # 3 hình dáng còn lại: List, Set, Hash
├─ PlaygroundController.java       #   REST: /queue, /visitors, /profiles
├─ EmailQueueService.java          #   List — hàng đợi email (LPUSH / RPOP)
├─ UniqueVisitorService.java       #   Set  — đếm visitor duy nhất (SADD / SCARD)
└─ PlayerProfileService.java       #   Hash — hồ sơ người chơi theo field (HSET / HGETALL)
```

Cấu hình hạ tầng: `docker-compose.yml` (Postgres + Redis + Redis Commander),
tham số app trong `src/main/resources/application.properties`.

## Chạy hạ tầng

```bash
docker compose up -d
```

- Postgres: `localhost:5432` (db/user/pass: `lab02`)
- Redis: `localhost:6379`
- Redis Commander (xem trực quan các "hình dáng" dữ liệu): http://localhost:8081

## Chạy app

```bash
./mvnw spring-boot:run
```

Lần chạy đầu tiên, app tự seed 1.000.000 người chơi ngẫu nhiên vào **cả** bảng
`player_score` (Postgres, cột `score` cố tình không đánh index) **lẫn** ZSet
`game_leaderboard` (Redis) — cùng một dữ liệu, khác cấu trúc lưu trữ, để so
sánh cho công bằng. Muốn đổi số lượng, sửa `app.leaderboard.seed-count`
trong `application.properties`.

## So sánh trực diện: ai lấy Top 10 nhanh hơn?

```bash
# Postgres: ORDER BY score DESC LIMIT 10 trên bảng không index -> phải quét + sort thật sự
curl http://localhost:8080/api/postgres-leaderboard/top10

# Redis: ZSet đã sẵn ở trạng thái sắp xếp -> luôn nhanh, không phụ thuộc N
curl http://localhost:8080/api/redis-leaderboard/top10
```

Mỗi response trả kèm `elapsedMs` để so sánh bằng số, không chỉ bằng lời.

```bash
# Cong diem cho mot nguoi choi (tuong duong ZADD)
curl -X POST "http://localhost:8080/api/redis-leaderboard/scores?player=Player-1&score=999999"

# Hoi ZSet: toi dang hang thu may, khong can quet ai ca
curl http://localhost:8080/api/redis-leaderboard/rank/Player-1
```

## Playground: 3 hình dáng còn lại

```bash
# List - hang doi email (LPUSH / RPOP)
curl -X POST "http://localhost:8080/api/queue/email?email=hello@example.com"
curl http://localhost:8080/api/queue/email/next

# Set - dem visitor duy nhat theo ngay (SADD / SCARD)
curl -X POST "http://localhost:8080/api/visitors/2026-08-18?userId=user-1"
curl http://localhost:8080/api/visitors/2026-08-18/count

# Hash - ho so nguoi choi, tung field rieng (HSET nhieu field / HGETALL)
curl -X POST http://localhost:8080/api/profiles/player-42 \
  -H "Content-Type: application/json" \
  -d '{"ten":"Rong Lua Can","tuoi":"27"}'
curl http://localhost:8080/api/profiles/player-42
```

Mở Redis Commander lên sau mỗi bước để tự tay nhìn thấy `email_queue` là
một List, `visitors:2026-08-18` là một Set, `profile:player-42` là một Hash —
và `game_leaderboard` là một ZSet đã sắp xếp sẵn.
