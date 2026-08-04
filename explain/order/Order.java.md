# Order.java

`src/main/java/com/example/redis_lab_prj/order/Order.java`

## Vai trò

Entity ánh xạ tới bảng `orders` — mỗi dòng là một lần đặt hàng, gắn với đúng 1 `Product`.

## Giải thích

- `@Table(name = "orders")`: đặt tên tường minh vì `order` là **từ khoá SQL** (ORDER BY) — nếu để Hibernate tự suy tên bảng từ `Order` → `order`, một số DB sẽ cần escape tên bảng ở mọi câu query, dễ gây lỗi khó hiểu. Đặt số nhiều `orders` tránh hẳn vấn đề này.
- `@ManyToOne(fetch = FetchType.LAZY, optional = false)` + `@JoinColumn(name = "product_id")`: một Order luôn thuộc về đúng 1 Product (thiết kế đã chốt ở `lab1-design.md` — không dùng `OrderItem` list). `FetchType.LAZY` nghĩa là khi load `Order` từ DB, Hibernate **chưa** load `Product` liên quan ngay — chỉ query khi code thực sự gọi `order.getProduct().something()`. Mặc định của `@ManyToOne` là `EAGER` (load kèm luôn), nhưng khai LAZY tường minh là thói quen tốt để tránh load dư dữ liệu không cần.
- `@Enumerated(EnumType.STRING)`: lưu `status` trong DB dưới dạng chuỗi (`"PENDING"`) thay vì số thứ tự (`0`, `1`, `2` — behavior mặc định `EnumType.ORDINAL`). Lý do: nếu sau này thêm/xoá/đổi thứ tự giá trị trong `OrderStatus` enum, dữ liệu cũ lưu dạng số sẽ bị đọc sai, dạng chuỗi thì an toàn.
- Constructor `Order(Product product, Integer quantity)` tự set `status = PENDING` — không có cách nào tạo Order mà thiếu status, tránh trạng thái `null` không hợp lệ.

## Điểm cần chú ý

`Order` không tự trừ `stock` của `Product` — việc đó nằm ở `OrderService`, không phải ở entity. Entity chỉ giữ dữ liệu và quan hệ, không chứa business rule về tồn kho.
