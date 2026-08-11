# GlobalExceptionHandler.java

`src/main/java/com/example/redis_lab_prj/common/exception/GlobalExceptionHandler.java`

## Vai trò

Nơi **duy nhất** trong toàn bộ app quyết định "exception nào → HTTP status nào → body JSON ra sao". Controller/Service chỉ cần `throw`, không cần tự bắt và tự format response lỗi.

## Giải thích

- `@RestControllerAdvice`: áp dụng cho **tất cả** `@RestController` trong app — không cần khai báo lại ở từng Controller. Đây là AOP (aspect-oriented): logic xử lý lỗi được "chèn" vào xung quanh mọi request mà không cần sửa code Controller.
- `@ExceptionHandler(X.class)`: khi bất kỳ Controller nào ném ra exception kiểu `X` (hoặc subclass), Spring tự động gọi method tương ứng ở đây thay vì để lỗi rơi xuống thành trang lỗi 500 mặc định.
- Mapping status code phản ánh đúng ngữ nghĩa HTTP:
  - `ResourceNotFoundException` → 404 Not Found
  - `InsufficientStockException` → 409 Conflict (request hợp lệ về mặt hình thức, nhưng xung đột với trạng thái hiện tại của server — còn hàng đâu mà đặt)
  - `MethodArgumentNotValidException` (lỗi `@Valid`) → 400 Bad Request (request client gửi sai định dạng/thiếu field)
- `handleValidation`: gom tất cả lỗi field validation thành 1 chuỗi (`"name must not be blank, price must be positive"`) thay vì chỉ trả lỗi đầu tiên — client sửa được nhiều lỗi cùng lúc thay vì sửa từng cái một qua nhiều lần gọi API.

## Vì sao không để mỗi Controller tự try/catch

Sẽ phải lặp lại đúng đoạn `try { ... } catch (ResourceNotFoundException e) { return ResponseEntity.status(404)... }` ở mọi method của `ProductController` và `OrderController` — trong khi cách xử lý là giống hệt nhau. Tập trung vào một nơi giúp thêm loại lỗi mới trong tương lai (vd. lỗi liên quan Redis ở Phase 2+) chỉ cần thêm 1 method ở đây.
