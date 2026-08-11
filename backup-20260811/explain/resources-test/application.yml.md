# application.yml (test)

`src/test/resources/application.yml`

## Vai trò

Cấu hình riêng cho lúc chạy `./mvnw test` — ghi đè hoàn toàn `spring.datasource.*` và `spring.jpa.*` của `src/main/resources/application.yml`, không merge từng phần.

## Giải thích

- `jdbc:h2:mem:redis_lab;MODE=PostgreSQL`: H2 chạy **trong RAM** (`mem:`), tự huỷ khi JVM test kết thúc — không cần Postgres cài sẵn trên máy hay CI runner để chạy test. `MODE=PostgreSQL` bật chế độ tương thích cú pháp SQL với Postgres (vd. cách xử lý kiểu dữ liệu, hàm built-in) để giảm rủi ro "chạy được trên H2 nhưng lỗi trên Postgres thật".
- `ddl-auto: create-drop`: tạo schema mới tinh lúc Spring context khởi động, xoá sạch lúc context đóng — mỗi lần chạy test là một DB sạch, test không bị ảnh hưởng bởi dữ liệu để lại từ lần chạy trước.
- `show-sql: false`: tắt log SQL khi chạy test — log test đã dài (JUnit, Spring context, Hibernate...), không cần thêm SQL log trừ khi đang debug một test cụ thể.

## Vì sao là ghi đè toàn bộ, không phải merge

Spring Boot ưu tiên `src/test/resources` trước `src/main/resources` khi có cùng tên file (`application.yml`) — file test thắng hoàn toàn cho những key nó khai báo lại, các key nó **không** khai (vd. `spring.application.name`) vẫn lấy từ file main. Đây là lý do file test không cần lặp lại `spring.application.name`.
