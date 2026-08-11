# OrderController.java

`src/main/java/com/example/redis_lab_prj/order/OrderController.java`

## Vai trò

Tầng HTTP cho Order. Cùng nguyên tắc với `ProductController` — xem `product/ProductController.java.md`.

## Giải thích

- `PATCH /api/orders/{id}/status`: dùng `@PatchMapping` thay vì `@PutMapping` vì đây là **cập nhật một phần** (chỉ đổi `status`), không thay thế toàn bộ resource. Theo đúng ngữ nghĩa HTTP: `PUT` = thay thế toàn bộ, `PATCH` = sửa một phần.
- Endpoint tách riêng path `/status` thay vì dùng chung `PUT /api/orders/{id}`: vì đổi status (huỷ đơn, hoàn kho) là một **hành động nghiệp vụ có tác dụng phụ** (side effect: hoàn `stock`), khác hẳn việc sửa thông tin thông thường — tách riêng để không ai vô tình đổi `status` khi chỉ định sửa field khác.

## Việc chưa làm

Không có endpoint xoá Order (`DELETE`). Trong nghiệp vụ order thực tế, xoá vĩnh viễn một đơn hàng thường không hợp lý bằng việc chuyển sang `CANCELLED` — giữ lại lịch sử thay vì mất dữ liệu.
