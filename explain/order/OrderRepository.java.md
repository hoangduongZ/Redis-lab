# OrderRepository.java

`src/main/java/com/example/redis_lab_prj/order/OrderRepository.java`

## Vai trò

Cổng giao tiếp giữa code Java và bảng `orders`. Tương tự `ProductRepository` — xem `product/ProductRepository.java.md` để hiểu cơ chế proxy của Spring Data JPA.

## Giải thích

Chưa có derived query nào (`findByStatus`, `findByProductId`...) vì Phase 1 chưa có nghiệp vụ nào cần lọc Order theo điều kiện — chỉ cần CRUD cơ bản qua `JpaRepository`. Thêm method vào interface này chỉ khi có yêu cầu thật, tránh đoán trước nhu cầu chưa xảy ra.
