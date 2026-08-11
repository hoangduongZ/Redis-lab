# OrderRequest.java

`src/main/java/com/example/redis_lab_prj/order/dto/OrderRequest.java`

## Vai trò

DTO cho JSON body của `POST /api/orders`.

## Giải thích

- `productId` + `quantity` — đúng 2 field cần thiết để tạo Order (đơn giản hoá theo thiết kế "1 Order = 1 Product + quantity").
- `@Positive` trên `quantity` (không phải `@PositiveOrZero` như `stock` bên Product): đặt hàng số lượng 0 là vô nghĩa về mặt nghiệp vụ — khác với `stock = 0` (hàng hết, vẫn hợp lệ để tồn tại).
- Không có field `status` trong request — client tạo Order không được quyền tự chọn trạng thái ban đầu, `OrderService` luôn gán `PENDING`. Đây là ranh giới rõ giữa "cái client được quyết định" và "cái hệ thống tự quyết định".
