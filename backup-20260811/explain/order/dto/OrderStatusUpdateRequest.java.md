# OrderStatusUpdateRequest.java

`src/main/java/com/example/redis_lab_prj/order/dto/OrderStatusUpdateRequest.java`

## Vai trò

DTO cho JSON body của `PATCH /api/orders/{id}/status` — chỉ chứa đúng 1 field.

## Giải thích

Tách riêng thành DTO độc lập (thay vì tái sử dụng `OrderRequest`) vì đây là **hành động khác hẳn** việc tạo order — request tạo cần `productId` + `quantity`, request đổi status chỉ cần `status`. Dùng chung một DTO cho 2 việc khác nhau sẽ dẫn tới các field không liên quan (`productId` trong lúc chỉ đổi status), gây hiểu nhầm API contract.

`@NotNull` trên `status`: nếu thiếu field này trong JSON, bị chặn ở tầng validation (400) trước khi vào tới `OrderService.updateStatus()` — Service không cần tự kiểm tra `null`.
