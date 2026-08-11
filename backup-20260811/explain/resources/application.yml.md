# application.yml (main)

`src/main/resources/application.yml`

## Vai trò

Cấu hình chạy thật của app — dùng khi chạy `./mvnw spring-boot:run` hoặc chạy trong Docker. Kết nối tới Postgres thật, không phải H2.

## Giải thích từng dòng

- `spring.datasource.url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:redis_lab}`: cú pháp `${DB_HOST:localhost}` là placeholder — đọc biến môi trường `DB_HOST`, nếu không có thì dùng giá trị mặc định sau dấu `:` (`localhost`). Đây là lý do cùng 1 file config chạy được cả khi dev local (không set env, dùng `localhost`) lẫn trong Docker Compose (set `DB_HOST=postgres` — tên service, xem `docker-compose.yml`).
- `driver-class-name: org.postgresql.Driver`: khai tường minh thay vì để Spring Boot tự đoán từ URL — tránh phụ thuộc vào cơ chế auto-detect, rõ ràng hơn khi đọc lại config sau này.
- `hibernate.ddl-auto: update`: Hibernate tự so sánh entity với schema hiện tại và **tự động** `ALTER TABLE` nếu thiếu cột/bảng. Tiện cho lab (không cần viết migration script tay), nhưng **không production-safe** — đã ghi rõ lý do và hướng thay thế (Flyway) trong `lab1-design.md`, mục 5 câu hỏi cuối Lab 1.
- `show-sql: true` + `format_sql: true`: in ra console câu SQL thật Hibernate sinh ra, có format dễ đọc — hữu ích để học (thấy rõ JPA dịch method call thành SQL gì), nhưng nên tắt (`false`) trong môi trường có traffic thật vì làm chậm và làm đầy log.
- `open-in-view: false`: tắt tính năng mặc định của Spring Boot là giữ Hibernate session mở tới tận lúc render xong response (Open Session In View). Tắt đi để buộc mọi truy vấn DB phải xảy ra tường minh trong tầng Service (transaction rõ ràng), tránh N+1 query ẩn xảy ra ở tầng Controller/serialization mà không ai kiểm soát — xem thêm lý do lazy-loading ở `order/dto/OrderResponse.java.md`.
