# OrderServiceTest.java

`src/test/java/com/example/redis_lab_prj/order/OrderServiceTest.java`

## Vai trò

Unit test cho `OrderService` — quan trọng nhất là xác nhận đúng công thức trừ/hoàn `stock`, vì đây là phần dễ sai và là trọng tâm của cả lab (xem `order/OrderService.java.md`).

## Giải thích

- Mock cả `OrderRepository` **và** `ProductRepository`: `OrderService` phụ thuộc cả hai (cần đọc/ghi `Product` để xử lý `stock`, và đọc/ghi `Order`) — test phải kiểm soát được cả hai để cô lập hoàn toàn logic, không phụ thuộc trạng thái DB thật.
- `create_decrementsStock_whenEnoughAvailable`: sau khi gọi `create`, assert trực tiếp trên **object `product` đã tạo trong test** (`product.getStock()` == 7) — không phải trên object trả về từ mock. Điều này đúng vì `OrderService.create()` gọi `product.setStock(...)` trực tiếp lên instance được `productRepository.findById()` trả về, tức chính là biến `product` trong test — nhờ vậy test verify được đúng cái mà code thật sự thay đổi.

## Các case được cover

- Happy path: đủ hàng → trừ đúng `stock`, tạo `Order` với status `PENDING`.
- Edge case: không đủ hàng → ném `InsufficientStockException`, **và** `stock` không bị thay đổi (`product.getStock()` vẫn là 2), **và** `orderRepository.save()` không được gọi — cả 3 assertion đều cần thiết để chắc chắn không có tác dụng phụ khi lỗi giữa chừng.
- Edge case: Product không tồn tại → `ResourceNotFoundException`.
- Happy path: huỷ Order (`CANCELLED`) → hoàn lại đúng `stock`.
- Edge case: đổi sang `CONFIRMED` (không phải huỷ) → `stock` không đổi, `productRepository.save()` không được gọi — đảm bảo logic hoàn kho chỉ chạy khi thực sự huỷ đơn.

## Case cố ý CHƯA cover: concurrent

Không có test nào giả lập 2 thread cùng gọi `create()` trên cùng 1 Product — vì mock đơn luồng (Mockito) không thể tái hiện race condition thật giữa 2 transaction Postgres. Việc này cần test tích hợp thật với DB thật và nhiều thread/request đồng thời — đúng là bài "break the system" ở `lab1-design.md`, dự kiến sẽ viết ở Phase 8 khi học Distributed Lock, không phải bây giờ.
