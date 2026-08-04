# ProductRepository.java

`src/main/java/com/example/redis_lab_prj/product/ProductRepository.java`

## Vai trò

Cổng giao tiếp duy nhất giữa code Java và bảng `products` trong Postgres.

## Giải thích

Chỉ là 1 interface trống kế thừa `JpaRepository<Product, Long>` — không có dòng code nào, nhưng đã có sẵn `save()`, `findById()`, `findAll()`, `delete()`, `deleteById()`...

Cơ chế: Spring Data JPA tại runtime tạo ra một **proxy class** implement interface này, tự sinh câu SQL dựa vào tên method và kiểu generic (`Product`, `Long` = kiểu id). Đây gọi là derived query — chưa dùng ở file này vì CRUD cơ bản không cần query tuỳ biến, nhưng là lý do sau này chỉ cần viết `findByStatus(OrderStatus status)` là có ngay câu `WHERE status = ?` mà không cần viết SQL tay.

## Vì sao không có `@Repository` annotation

`JpaRepository` đã được Spring Data tự động phát hiện và đăng ký bean qua cơ chế riêng (không dựa vào `@ComponentScan` như `@Service`/`@Controller`) — chỉ cần interface nằm trong package được Spring Boot scan là đủ.
