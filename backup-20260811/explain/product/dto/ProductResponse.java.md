# ProductResponse.java

`src/main/java/com/example/redis_lab_prj/product/dto/ProductResponse.java`

## Vai trò

DTO đại diện cho JSON trả về ở mọi endpoint liên quan tới Product — "hình dạng dữ liệu đi ra".

## Giải thích

- Field đều `final`, chỉ có getter, không có setter — vì đây là dữ liệu đọc, một khi đã tạo ra để trả response thì không có lý do gì bị sửa lại giữa chừng.
- `static ProductResponse from(Product product)`: factory method chuyển từ entity sang DTO. Đặt static method ngay trong DTO (thay vì để rải rác ở Controller) để có **một chỗ duy nhất** định nghĩa "entity map sang JSON thế nào" — nếu sau này thêm field vào response, chỉ sửa một nơi.

## Vì sao không convert bằng tay trong Controller

Nếu để `ProductController` tự viết `new ProductResponse(product.getId(), product.getName(), ...)`, mỗi endpoint (create, findAll, findById, update) sẽ lặp lại đúng đoạn code này — vi phạm nguyên tắc không lặp lại logic giống nhau nhiều nơi.
