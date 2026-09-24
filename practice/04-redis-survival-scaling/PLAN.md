# Kế Hoạch Practice — Bài 4: Sự Sinh Tồn Và Đội Quân Nhân Bản (Persistence & Scaling)

> Bám sát [`giao-an/04-redis-survival-scaling.md`](../../giao-an/04-redis-survival-scaling.md). Trọng tâm: **Persistence** (RDB vs AOF) và **Replication + Sentinel failover**. Đây là bài cuối của khóa học — không dựng Redis Cluster thật, chỉ cần hiểu khái niệm "chia để trị" bằng lời (xem mục 6).

## 1. Mục tiêu practice

Tự tay cảm nhận Redis vừa "mong manh" (RAM) vừa "bất tử" (nhờ cơ chế sao lưu + nhân bản) như giáo án mô tả:
1. Rút phích cắm điện thật sự (`docker kill -s SIGKILL`) một Redis chỉ bật RDB mặc định → mất trắng các dòng ghi gần nhất, vì chưa kịp tới điểm chụp ảnh (snapshot) kế tiếp.
2. Làm y hệt với một Redis bật AOF → cúp điện xong bật lại, dữ liệu còn nguyên 100%, vì mọi lệnh ghi đã được chép vào sổ nhật ký.
3. Dựng 1 Master + 2 Replica, ghi vào Master, đọc từ 2 Replica → thấy dữ liệu tự động chảy sang; thử ghi thẳng vào Replica → bị Redis chặn (lỗi READONLY).
4. Rút phích Master khi có Sentinel đứng gác → thấy Sentinel tự bầu 1 Replica cũ lên làm Master mới trong vài giây, và app Spring Boot ghi tiếp được ngay mà KHÔNG cần sửa code hay restart.

## 2. Tech stack

| Thành phần | Lựa chọn | Vì sao |
|---|---|---|
| Backend | Java 17 + Spring Boot 3 | nối tiếp bài 1–3, chỉ dùng cho phần Replication/Sentinel — nơi cần chứng minh "app tự kết nối lại" thật |
| Phần Persistence | Thao tác thẳng bằng `redis-cli` qua `docker exec` + script, KHÔNG viết API Java | bài học nằm ở vòng đời tiến trình (kill/start) và file trên đĩa, chèn thêm 1 tầng API chỉ làm rối trọng tâm |
| Redis client (Replication) | Lettuce (mặc định của Spring Boot) với `RedisSentinelConfiguration` | đúng cơ chế "tự tìm Master hiện tại qua Sentinel" mà giáo án mô tả, không tự chế logic retry/reconnect |
| Hạ tầng Persistence | 2 container Redis riêng: `redis-rdb-only` (chỉ RDB, `save` mặc định) và `redis-aof` (`appendonly yes`) | tách biệt để so sánh song song trên cùng kịch bản kill điện |
| Hạ tầng Replication | Docker Compose: 1 `redis-master` + 2 `redis-replica` (`replicaof`) + 3 `redis-sentinel` (`sentinel monitor mymaster ... 2`) | quorum 2/3 đúng tinh thần "anh lính gác" cần đa số đồng ý mới bầu Master mới |
| Volume | mount volume riêng cho từng Redis container (không dùng tmpfs) | dữ liệu phải sống sót qua `docker kill` + `docker start` lại **đúng container đó** — mô phỏng "cúp điện rồi có điện lại", không phải dựng máy mới |

## 3. Cấu trúc project dự kiến

```
practice/04-redis-survival-scaling/
├── PLAN.md
└── app/
    ├── docker-compose.yml         # redis-rdb-only, redis-aof, redis-master, redis-replica-1/2, redis-sentinel-1/2/3
    ├── pom.xml
    ├── src/main/java/com/redislab/lab04/
    │   ├── Lab04Application.java
    │   └── replication/
    │       ├── ReplicationConfig.java        # LettuceConnectionFactory qua RedisSentinelConfiguration (mymaster, 3 sentinel host:26379-26381) + 2 RedisTemplate riêng trỏ thẳng từng replica (port cố định) để đọc/test ghi trực tiếp
    │       ├── ReplicationController.java     # ghi qua template sentinel-aware, đọc/ghi thử thẳng từng replica
    │       └── SentinelStatusController.java  # hỏi Sentinel "ai đang là Master" — thấy địa chỉ đổi sau failover
    └── scripts/
        ├── simulate-outage-rdb.sh     # ghi vài key vào redis-rdb-only, kill -9 ngay (trước checkpoint), start lại, đọc lại → chứng minh mất
        ├── simulate-outage-aof.sh     # y hệt nhưng trên redis-aof → chứng minh còn nguyên
        └── simulate-master-failover.sh # docker kill redis-master, đợi vài giây, gọi SentinelStatusController xem Master mới, ghi tiếp qua ReplicationController → vẫn thành công
```

### Endpoint dự kiến

**Replication (chỉ phần này cần API, phần Persistence dùng thẳng `redis-cli`):**
- `POST /api/replication/write?key=&value=` — ghi qua template trỏ Sentinel (`mymaster`), Lettuce tự tìm đúng Master hiện tại, kể cả sau failover
- `GET /api/replication/read?node=replica-1|replica-2&key=` — đọc thẳng 1 Replica cụ thể để thấy dữ liệu đã tự chảy sang từ Master
- `POST /api/replication/try-write-to-replica?node=replica-1|replica-2&key=&value=` — cố tình ghi thẳng vào Replica → bắt lỗi `READONLY` từ Redis và trả về, để thấy tận mắt "chỉ Master được viết"
- `GET /api/replication/sentinel/current-master` — hỏi Sentinel địa chỉ Master hiện tại (`SENTINEL get-master-addr-by-name mymaster`)

## 4. Các bước triển khai

1. **Persistence trước — gây "nỗi đau" bằng số liệu thật, không lý thuyết suông**:
   - `docker-compose`: `redis-rdb-only` (`redis:7-alpine`, giữ `save` mặc định, mount `./data/rdb:/data`) và `redis-aof` (`command: redis-server --appendonly yes --appendfsync everysec`, mount `./data/aof:/data`).
   - Ghi vài key vào cả 2 bằng `docker exec <container> redis-cli SET k1 v1`.
   - `docker kill -s SIGKILL <container>` ngay lập tức (không đợi tới lần save kế tiếp), rồi `docker start <container>`.
   - `redis-cli GET` lại → `redis-rdb-only` thiếu key vừa ghi, `redis-aof` đủ 100%.
2. **Replication**: thêm `redis-master`, `redis-replica-1` (`command: redis-server --replicaof redis-master 6379`), `redis-replica-2` tương tự. Test tay bằng `redis-cli` trước khi động tới Java: ghi vào master, đọc ở 2 replica → đồng bộ; ghi thẳng vào replica → nhận lỗi `READONLY`.
3. **Sentinel**: thêm 3 service `redis-sentinel-1/2/3` (`redis-sentinel` với `sentinel monitor mymaster redis-master 6379 2`, quorum = 2). `ReplicationConfig` dùng `RedisSentinelConfiguration` trỏ 3 sentinel — Spring Boot tự tạo `LettuceConnectionFactory` biết hỏi Sentinel để tìm Master, không tự viết logic tìm kiếm.
4. **Failover thật**: gọi `POST /api/replication/write` vài lần chắc chắn thành công → `docker kill redis-master` (rút phích thật, không phải `docker stop` êm ái) → đợi vài giây (`down-after-milliseconds`) → gọi `GET /api/replication/sentinel/current-master` thấy địa chỉ đổi sang 1 trong 2 replica cũ → gọi lại `POST /api/replication/write` KHÔNG sửa code, KHÔNG restart app → vẫn ghi thành công vào Master mới. Đây là khoảnh khắc "aha" của cả bài.
5. **Đóng khóa học**: đọc lại giáo án mục 5 (Cluster) để hiểu khái niệm, KHÔNG dựng cluster thật (xem mục 6).

## 5. Tiêu chí hoàn thành

- [ ] `redis-rdb-only` mất dữ liệu ghi ngay trước khi bị `docker kill -9` (chưa tới checkpoint save).
- [ ] `redis-aof` giữ nguyên 100% dữ liệu sau `docker kill -9` + `docker start` lại, dù ghi ngay trước khi kill.
- [ ] Ghi vào Master, đọc thấy dữ liệu ở cả 2 Replica.
- [ ] Ghi thẳng vào 1 Replica bị Redis từ chối với lỗi READONLY.
- [ ] Sentinel (quorum 2/3) tự bầu 1 Replica cũ lên làm Master mới trong vòng vài giây sau khi `docker kill redis-master`.
- [ ] App Spring Boot ghi tiếp được vào Master mới mà KHÔNG cần sửa code hay restart — chứng minh Sentinel-aware client hoạt động thật.
- [ ] Không dựng Redis Cluster thật — hiểu khái niệm qua giáo án là đạt.

## 6. Việc KHÔNG làm ở bài này

- Không dựng Redis Cluster thật (6+ node, hash slot) — bài học cuối chỉ cần hiểu khái niệm "chia để trị" bằng lời qua giáo án, không cần vận hành hạ tầng multi-node phức tạp.
- Không benchmark hiệu năng đọc/ghi (số req/s, độ trễ chi tiết) — trọng tâm là "thấy được cơ chế chạy đúng", không phải đo hiệu năng.
- Không TLS/ACL/authentication nâng cao cho Redis — giữ cấu hình tối giản để không rối trọng tâm bài học.
- Không tự viết logic retry/reconnect thủ công khi Master đổi — để Lettuce + Sentinel làm đúng vai trò của nó (đúng tinh thần bài 3: không tự chế cái mà thư viện/nền tảng đã lo).
