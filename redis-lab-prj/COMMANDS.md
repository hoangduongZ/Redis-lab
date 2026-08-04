# Lệnh cho project

Chạy từ thư mục `redis-lab-prj/`. Windows dùng `mvnw.cmd`, Git Bash dùng `./mvnw`.

## Maven

**`./mvnw compile`**

> Compile code, chưa chạy test.
> Dùng để check nhanh lỗi syntax sau khi sửa 1 class.


**`./mvnw test`**

> Chạy toàn bộ test (unit test + @WebMvcTest).
> Dùng H2 in-memory, không cần Postgres đang chạy.
> Dùng sau mỗi lần sửa logic, trước khi commit.


**`./mvnw clean package -DskipTests`**

> Build ra file `.jar` trong `target/`, bỏ qua test.
> Dùng khi cần build nhanh để chạy thử hoặc build Docker image.


**`./mvnw spring-boot:run`**

> Chạy app trực tiếp bằng Maven.
> Cần Postgres đang chạy trước (`docker compose up -d postgres` bên dưới),
> vì `application.yml` trỏ tới `localhost:5432`.

## Docker

**`docker compose up -d postgres`**

> Chỉ chạy Postgres.
> Dùng khi muốn code/debug app bằng IDE (`spring-boot:run`) và chỉ cần DB có sẵn.


**`docker compose up --build`**

> Build lại image và chạy cả `app` + `postgres`.
> Dùng khi muốn test toàn bộ hệ thống giống môi trường thật.


**`docker compose down`**

> Dừng và xoá container.
> Data trong Postgres vẫn giữ (nằm trong volume `postgres-data`).


**`docker compose down -v`**

> Dừng và xoá luôn volume — mất hết data trong Postgres.
> Chỉ dùng khi muốn reset sạch.
