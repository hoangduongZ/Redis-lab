# ResourceNotFoundException.java

`src/main/java/com/example/redis_lab_prj/common/exception/ResourceNotFoundException.java`

## Vai trò

Exception dùng chung cho mọi trường hợp "tìm không thấy resource theo id" — Product không tồn tại, Order không tồn tại...

## Giải thích

Kế thừa `RuntimeException` (unchecked) chứ không phải `Exception` (checked): checked exception bắt buộc mọi method gọi nó phải khai `throws` hoặc `try/catch` — với một lỗi phổ biến như "not found" xảy ra ở rất nhiều method trong `ProductService`/`OrderService`, bắt buộc khai báo `throws` khắp nơi sẽ chỉ làm code rối mà không thêm giá trị, vì cách xử lý ở mọi nơi đều giống nhau (trả 404) — đã có `GlobalExceptionHandler` lo việc đó tập trung một chỗ.

## Vì sao dùng chung 1 exception cho cả Product và Order

Cả hai đều là cùng một loại lỗi về bản chất ("không tìm thấy entity theo id"), khác nhau chỉ ở nội dung message (`"Product not found: " + id` vs `"Order not found: " + id`). Tạo `ProductNotFoundException` và `OrderNotFoundException` riêng sẽ là 2 class chỉ khác tên, không có giá trị xử lý khác biệt nào — vi phạm nguyên tắc không tạo abstraction khi chưa cần.
