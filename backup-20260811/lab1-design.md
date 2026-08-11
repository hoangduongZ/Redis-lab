# Lab 1 — CRUD Backend Foundation

**Stack:** Spring Boot + PostgreSQL (source of truth). Chưa dùng Redis ở lab này.

## Bối cảnh

Đây là điểm khởi đầu của toàn bộ roadmap. Trước khi Redis xuất hiện ở Phase 2, chúng ta cần một backend CRUD chạy được, có test, có Docker — để các phase sau (cache, rate limit, leaderboard, lock...) có nền để build lên.

Kiến trúc hiện tại:

```
Backend (Spring Boot)
        ↓
   PostgreSQL
```

## Việc cần làm ở Lab 1

* PostgreSQL CRUD
* API cơ bản
* Test
* Docker

Chưa viết code. Trước khi đụng tới Spring Boot hay PostgreSQL, cần thiết kế trước.

## Câu hỏi thiết kế đầu tiên

Trước khi có bất kỳ cache nào, bản thân nghiệp vụ CRUD này phải hợp lý đã. Vậy:

**Bạn chọn domain/entity nào để xuyên suốt cả lab (Lab 1 → Lab 11)?**

Ví dụ: `Product`, `Order`, `Article`, `User`... Domain này sẽ là thứ bạn:

* Query nhiều lần → sẽ cần cache ở Phase 2-3
* Update thường xuyên → sẽ cần xử lý cache invalidation
* Có thể có counter (view count, stock...) → sẽ cần INCR ở Phase 4
* Có thể xếp hạng (điểm, giá, rating...) → sẽ cần Sorted Set ở Phase 7

Chọn domain nào cũng được, nhưng nên là thứ đủ "thật" để các vấn đề ở phase sau (cache stampede, rate limit, leaderboard) áp dụng tự nhiên, không bị gượng.

Sau khi bạn chọn domain, hãi trả lời tiếp:

1. Entity đó có những field nào (tối thiểu, đừng over-design)?
2. API cần những endpoint CRUD nào — bạn nghĩ cần đủ 5 method (Create/Read one/Read list/Update/Delete), hay có thể bỏ bớt ở giai đoạn này?
3. Bạn định tổ chức layer trong Spring Boot thế nào — Controller/Service/Repository, hay có ý tưởng khác?

Trả lời xong phần domain + field trước, rồi mình review, sau đó mới sang câu hỏi về API layer.

## Quyết định đã chốt

* **Domain:** E-commerce — `Product` + `Order` (Product có field `stock`)
* **API:** thiết kế đầy đủ CRUD, không cắt giảm
* **Layer:** Controller / Service / Repository

## Câu hỏi thiết kế tiếp theo

Trước khi vẽ endpoint, cần chốt shape của data đã — vì API chỉ là cái vỏ bọc quanh entity.

Bạn tự thiết kế và trả lời:

1. **`Product`** có những field nào? (tối thiểu để chạy được CRUD + đủ cho `stock` dùng ở Phase 8 sau này — đừng thêm field "cho đẹp" nếu chưa cần)
2. **`Order`** có những field nào, và quan hệ với `Product` ra sao?
   * Một Order chỉ đặt 1 Product với 1 quantity? Hay Order có nhiều OrderItem (nhiều Product)?
   * Đây là quyết định ảnh hưởng đến độ phức tạp của toàn bộ lab — chọn đơn giản trước, có thể mở rộng sau.
3. Với 2 entity đó, bạn nghĩ cần những endpoint gì? Liệt kê method + path (chưa cần request/response body chi tiết).

Trả lời xong, mình review thiết kế trước khi bạn động tới Spring Boot.

## Data model & API (ngoại lệ — quyết định bởi mentor theo yêu cầu của user, không phải quy trình chuẩn)

### Product

| field | type | lý do |
|---|---|---|
| id | Long | PK |
| name | String | bắt buộc |
| description | String (nullable) | optional |
| price | BigDecimal | cần cho nghiệp vụ order |
| stock | Integer | trọng tâm — dùng cho race condition/distributed lock ở Phase 8 |
| createdAt / updatedAt | Instant | audit |

Không đưa `viewCount` vào Postgres — đó là counter, đúng vị trí của nó là Redis `INCR` ở Phase 4. Thêm vào DB bây giờ là tối ưu sớm cho thứ chưa cần.

### Order

| field | type | lý do |
|---|---|---|
| id | Long | PK |
| productId | Long (FK) | 1 Order = 1 Product + quantity — **không** dùng OrderItem list, giữ đơn giản |
| quantity | Integer | số lượng đặt |
| status | Enum: PENDING, CONFIRMED, CANCELLED | mở đường cho Phase 6 (order pipeline qua Stream/Queue) |
| createdAt | Instant | audit |

Quan hệ đơn giản (1 Product/Order) vẫn đủ để tạo race condition thật: nhiều Order cùng trừ `stock` của 1 Product → dùng cho Phase 8.

### Endpoints

**Product**
- `POST /api/products`
- `GET /api/products`
- `GET /api/products/{id}`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

**Order**
- `POST /api/orders` — tạo order, trừ `stock` của Product tương ứng (action này sẽ là "hiện trường" concurrency issue ở Phase 8)
- `GET /api/orders`
- `GET /api/orders/{id}`
- `PATCH /api/orders/{id}/status` — đổi status; nếu huỷ (CANCELLED) thì cộng lại `stock`

## Bước tiếp theo

Quay lại quy trình chuẩn: bạn tự thiết kế package structure Controller/Service/Repository (tên class, method signature) trước khi viết code thật.

## Scaffold đã tạo (Base Scaffolding Exception)

Project tại `redis-lab-prj/`, Spring Boot 4.1.0 / Java 21, code style Java 11 (không dùng record theo yêu cầu).

* `product/` — `Product`, `ProductRepository`, `ProductService`, `ProductController`, `dto/ProductRequest`, `dto/ProductResponse`
* `order/` — `Order`, `OrderStatus`, `OrderRepository`, `OrderService`, `OrderController`, `dto/OrderRequest`, `dto/OrderResponse`, `dto/OrderStatusUpdateRequest`
* `common/exception/` — `ResourceNotFoundException`, `InsufficientStockException`, `ApiError`, `GlobalExceptionHandler`
* `docker-compose.yml` + `Dockerfile` — Postgres + app
* Test: `ProductServiceTest`, `OrderServiceTest` (Mockito), `ProductControllerTest` (`@WebMvcTest`) — happy path + edge case (not found, insufficient stock) + failure case (validation lỗi)
* `COMMANDS.md` — lệnh `mvnw`/`docker compose` và khi nào dùng

`./mvnw test` chạy xanh (dùng H2 in-memory, không cần Postgres).

**Điểm quan trọng cần hiểu, không phải chỉ chạy được là xong:** `OrderService.create()` trừ `stock` bằng cách đọc rồi ghi lại (`product.getStock() - quantity`) — **không có khoá nào bảo vệ**. Đây chính là race condition sẽ dùng để học Distributed Lock ở Phase 8. Đừng tự ý "sửa" nó ngay bây giờ.

## Break the system (Phase 1)

Scenario: 2 request `POST /api/orders` cùng đặt 1 Product có `stock = 1`, gửi gần như đồng thời (`quantity = 1` mỗi request).

Bạn dự đoán: kết quả `stock` cuối cùng sẽ là bao nhiêu? Có khả năng nào cả 2 order đều tạo thành công (dù chỉ còn 1 hàng) không? Tại sao?

## 5 câu hỏi cuối Lab 1

1. Vì sao Postgres đủ cho toàn bộ Phase 1 — Redis chưa cần thiết ở đây?
-> app chạy ok trước đã trước khi thực hành với redis, trong lúc chạy app phát sinh case gì cần dùng redis thì dùng, bản chất là gặp vấn đề nào xử lí vấn đề đấy, vì redis sinh ra cũng là giải quyết 1 nỗi đau gì đấy
2. Entity/API design (Product/Order, 1 Order = 1 Product) có hợp lý không, hay đang thiếu gì cho các phase sau?
3. Nếu 100 request `POST /api/orders` tới cùng lúc trên 1 Product, hệ thống hiện tại phản ứng ra sao?
4. `ddl-auto: update` có phải lựa chọn production-safe không? Vì sao lab này tạm chấp nhận nó?
-> đây không phải production-safe vì sửa code ảnh hưởng đến database, sẽ không kiểm soát được, kiểm soát trực tiếp qua sql sẽ dễ dàng hơn, hoặc fly migrate có version rõ ràng
5. Nếu traffic đọc `GET /api/products` tăng 100x, điểm nghẽn đầu tiên là gì?
