# ProductController.java

`src/main/java/com/example/redis_lab_prj/product/ProductController.java`

## Vai trò

Tầng HTTP — nhận request, gọi Service, trả response. Không chứa business logic.

## Giải thích

- `@RestController` = `@Controller` + `@ResponseBody`: mọi method trả về sẽ tự động serialize thành JSON (qua Jackson) thay vì Spring tìm view template để render.
- `@RequestMapping("/api/products")`: prefix chung cho cả class, mỗi method chỉ cần khai path còn lại.
- `@Valid @RequestBody ProductRequest request`: `@RequestBody` bảo Jackson deserialize JSON trong body thành `ProductRequest`; `@Valid` kích hoạt Bean Validation (các annotation `@NotBlank`, `@PositiveOrZero`... trong `ProductRequest`) — nếu vi phạm, ném `MethodArgumentNotValidException` trước khi vào tới thân method, được `GlobalExceptionHandler` bắt và trả 400.
- `@ResponseStatus(HttpStatus.CREATED)` / `NO_CONTENT`: khai báo mã trạng thái HTTP đúng chuẩn REST (201 cho tạo mới, 204 cho xoá — không có body trả về), thay vì mặc định 200 cho tất cả.
- Controller nhận `ProductRequest`, trả `ProductResponse` — không dùng trực tiếp entity `Product`. Đây là ranh giới giữa "hình dạng dữ liệu API" và "hình dạng dữ liệu lưu trữ", hai thứ được phép khác nhau (vd. entity có `updatedAt` phục vụ audit nhưng API có thể không cần lộ ra, hoặc field JSON đặt tên khác field DB).
