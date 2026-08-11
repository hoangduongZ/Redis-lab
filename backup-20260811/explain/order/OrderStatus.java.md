# OrderStatus.java

`src/main/java/com/example/redis_lab_prj/order/OrderStatus.java`

## Vai trò

Enum liệt kê toàn bộ trạng thái hợp lệ của một Order: `PENDING`, `CONFIRMED`, `CANCELLED`.

## Giải thích

Dùng enum thay vì `String status` tự do (vd. cho phép bất kỳ chuỗi nào) vì:

- Compiler chặn giá trị sai ngay lúc build (`OrderStatus.SHIPED` gõ sai sẽ không compile được, còn `"SHIPED"` dạng String thì compile bình thường, chỉ lỗi lúc chạy).
- Không cần validate "status có nằm trong danh sách hợp lệ không" bằng tay ở Service — kiểu dữ liệu đã tự đảm bảo điều đó.

## Vì sao chỉ 3 trạng thái

Đủ cho Phase 1 (tạo → huỷ). Chưa thêm `SHIPPED`, `DELIVERED`... vì chưa có nghiệp vụ nào trong lab hiện tại cần tới — sẽ mở rộng khi Phase 6 (order pipeline qua Stream/Queue) thực sự cần phân biệt thêm trạng thái xử lý.
