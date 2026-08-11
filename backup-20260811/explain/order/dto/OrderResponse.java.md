# OrderResponse.java

`src/main/java/com/example/redis_lab_prj/order/dto/OrderResponse.java`

## Vai trò

DTO JSON trả về cho mọi endpoint liên quan Order.

## Giải thích

- `productId` lấy từ `order.getProduct().getId()` chứ không trả nguyên object `Product` lồng bên trong. Lý do:
  1. Tránh vòng lặp vô hạn khi serialize nếu sau này `Product` có tham chiếu ngược lại `Order`.
  2. Response gọn — client cần biết Order thuộc Product nào (qua id) để tự gọi `GET /api/products/{id}` nếu cần chi tiết, không nhất thiết phải nhúng toàn bộ Product vào mọi response Order.
- Gọi `order.getProduct().getId()` sẽ kích hoạt query lazy-load `Product` (xem `order/Order.java.md` về `FetchType.LAZY`) nếu Product chưa được load sẵn trong transaction hiện tại — đây là lý do `OrderResponse.from()` phải được gọi **trong lúc transaction/session còn mở** (tức là trong Controller, ngay sau khi Service trả về), không phải sau khi response đã rời khỏi tầng Service.
