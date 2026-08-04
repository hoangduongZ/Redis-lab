# OrderService.java

`src/main/java/com/example/redis_lab_prj/order/OrderService.java`

## Vai trò

Business logic của Order — nơi duy nhất được phép trừ/hoàn `stock` của Product.

## Giải thích

- `create()`: tìm Product theo `productId` → kiểm tra đủ hàng không → trừ `stock` → lưu Product → tạo Order mới với status `PENDING` → lưu Order.
- `@Transactional` trên `create()` và `updateStatus()`: đảm bảo các bước ghi (`productRepository.save`, `orderRepository.save`) xảy ra trong **cùng một database transaction** — nếu bước sau lỗi (vd. lưu Order thất bại), bước trừ `stock` trước đó cũng bị rollback, không để lại trạng thái nửa vời (trừ kho nhưng không có Order tương ứng).
- `updateStatus()`: nếu chuyển sang `CANCELLED` mà trạng thái hiện tại chưa phải `CANCELLED`, hoàn lại `stock` bằng đúng `quantity` đã trừ lúc tạo. Điều kiện `order.getStatus() != OrderStatus.CANCELLED` chặn việc hoàn kho 2 lần nếu ai đó gọi cancel một Order đã cancel rồi.

## ⚠️ Đây là "hiện trường" quan trọng nhất của cả lab

Đoạn:

```java
if (product.getStock() < request.getQuantity()) { ... }
product.setStock(product.getStock() - request.getQuantity());
productRepository.save(product);
```

là **đọc rồi ghi lại** (read-modify-write), **không có khoá nào bảo vệ**. `@Transactional` ở đây chỉ đảm bảo tính toàn vẹn của *transaction* (rollback nếu lỗi), **không** ngăn được 2 transaction khác nhau cùng đọc `stock = 1` tại cùng một thời điểm rồi cùng trừ xuống `0` — tức là bán vượt tồn kho.

Đây chính là race condition cố ý để lại, sẽ dùng để học Distributed Lock ở Phase 8 (xem `lab1-design.md` mục "Break the system"). Không tự ý thêm `synchronized`, `@Version` (optimistic locking), hay `SELECT FOR UPDATE` vào đây bây giờ — thứ này cần được *phát hiện* qua test trước khi được *sửa* có chủ đích ở đúng phase của nó.
