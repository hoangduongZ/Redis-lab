# Product.java

`src/main/java/com/example/redis_lab_prj/product/Product.java`

## Vai trò

Entity ánh xạ tới bảng `products` trong Postgres — nguồn sự thật (source of truth) cho toàn bộ dữ liệu sản phẩm.

## Giải thích

- `@Entity` + `@Table(name = "products")`: báo cho Hibernate biết class này map với bảng nào. Không có `@Table` thì Hibernate tự suy ra tên bảng từ tên class (`Product` → `product`), khai báo tường minh để tránh phụ thuộc vào naming convention ngầm định.
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`: khoá chính tự tăng, giao cho Postgres sinh ID (`SERIAL`/`IDENTITY` column) thay vì Hibernate tự quản lý sequence riêng — đơn giản và đúng với cách Postgres vẫn hay dùng.
- `stock`: field trọng tâm của cả lab — không chỉ là số lượng tồn kho, mà là nơi sẽ xảy ra race condition khi nhiều `Order` cùng trừ vào nó (xem `OrderService.java.md`).
- `protected Product()`: constructor rỗng bắt buộc phải có để Hibernate tạo instance qua reflection khi load dữ liệu từ DB. Để `protected` (không phải `public`) để code nghiệp vụ không vô tình new ra một Product rỗng, không hợp lệ.
- `@PrePersist` / `@PreUpdate`: hook lifecycle của JPA, tự set `createdAt`/`updatedAt` ngay trước khi Hibernate insert/update — không cần tự gọi tay ở Service, tránh quên sót.

## Vì sao không dùng record

Entity JPA bắt buộc phải có constructor rỗng + setter để Hibernate proxy/reflection hoạt động (lazy loading, dirty checking) — record (immutable, không setter) không tương thích với cơ chế này. Đây là lý do JPA entity luôn là class thường, kể cả trong codebase dùng Java 17+.
