# ProductRequest.java

`src/main/java/com/example/redis_lab_prj/product/dto/ProductRequest.java`

## Vai trò

DTO (Data Transfer Object) đại diện cho JSON body của request tạo/sửa Product. Đây là "hình dạng dữ liệu đi vào", tách biệt khỏi entity `Product`.

## Giải thích

- Là class thường (không phải record) theo yêu cầu giữ code ở mức kiến thức Java 11 — có constructor rỗng (Jackson cần để deserialize), constructor đầy đủ field, và getter/setter thường.
- `@NotBlank`, `@NotNull`, `@PositiveOrZero`: annotation của Jakarta Bean Validation. Chỉ có tác dụng khi Controller khai `@Valid` trước tham số — bản thân annotation không tự kiểm tra gì cả, cần một validator (Hibernate Validator, đến từ `spring-boot-starter-validation`) đọc và thực thi.
- `price`/`stock` dùng `@PositiveOrZero` chứ không phải `@Positive`: cho phép giá trị 0 (sản phẩm hết hàng vẫn hợp lệ để tồn tại trong hệ thống, giá 0 có thể là hàng khuyến mãi) — đây là quyết định nghiệp vụ, không phải giới hạn kỹ thuật.

## Vì sao tách riêng khỏi `Product` entity

Nếu Controller nhận thẳng `Product`, client có thể gửi `id`, `createdAt`, `updatedAt` trong JSON và ghi đè các field mà lẽ ra chỉ hệ thống được quyền set — DTO chặn việc đó bằng cách chỉ định nghĩa field client được phép gửi.
