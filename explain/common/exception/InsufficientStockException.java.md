# InsufficientStockException.java

`src/main/java/com/example/redis_lab_prj/common/exception/InsufficientStockException.java`

## Vai trò

Exception ném ra khi `OrderService.create()` phát hiện `stock` không đủ cho `quantity` yêu cầu.

## Giải thích

Tách riêng khỏi `ResourceNotFoundException` vì đây là **loại lỗi khác về bản chất**, cần HTTP status khác:

- Not found → **404** (resource không tồn tại).
- Insufficient stock → **409 Conflict** (resource tồn tại, nhưng request xung đột với trạng thái hiện tại của nó — xem `GlobalExceptionHandler.java.md`).

Nếu dùng chung 1 exception cho cả 2 trường hợp, `GlobalExceptionHandler` sẽ không thể phân biệt để trả đúng status code — đây là ranh giới hợp lý để tách class, khác với trường hợp `ResourceNotFoundException` dùng chung cho Product/Order (cùng bản chất, cùng status code).
