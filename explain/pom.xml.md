# pom.xml

`redis-lab-prj/pom.xml`

## Vai trò

Khai báo project cho Maven: build với JDK nào, phụ thuộc (dependency) nào, build ra artifact gì.

## Giải thích

- `<parent><artifactId>spring-boot-starter-parent</artifactId><version>4.1.0</version></parent>`: kế thừa một POM cha do Spring Boot cung cấp — POM cha này định nghĩa sẵn **version tương thích** cho hàng trăm thư viện phổ biến (Jackson, Hibernate, JUnit...) nên các dependency bên dưới không cần tự khai `<version>` — tránh việc tự chọn version lệch nhau gây xung đột (dependency hell).
- `<java.version>21</java.version>`: property này được `spring-boot-starter-parent` đọc để set target/source bytecode cho compiler plugin — chỉ cần đổi 1 dòng này thay vì tự cấu hình `maven-compiler-plugin` bằng tay.
- Từng `<dependency>` — vì sao có mặt:
  - `spring-boot-starter-web`: REST controller, embedded Tomcat, Jackson để serialize JSON.
  - `spring-boot-starter-data-jpa`: Hibernate + Spring Data repository (`JpaRepository`).
  - `spring-boot-starter-validation`: kích hoạt annotation `@NotBlank`, `@Positive`... trong các DTO.
  - `postgresql` (`scope: runtime`): driver JDBC thật. `runtime` nghĩa là không cần thư viện này lúc **compile** code (code Java không import gì từ nó trực tiếp — chỉ cấu hình URL `jdbc:postgresql://...`), chỉ cần có mặt lúc **chạy**.
  - `h2` (`scope: test`): DB in-memory, chỉ dùng khi chạy test (`src/test/resources/application.yml`) — không có mặt trong jar chạy production, giữ artifact production gọn.
  - `spring-boot-webmvc-test` + `spring-boot-starter-test` (`scope: test`): cung cấp JUnit 5, Mockito, AssertJ, MockMvc, và annotation `@WebMvcTest` (Spring Boot 4 tách `@WebMvcTest` ra module riêng thay vì gộp chung trong `spring-boot-starter-test` như bản cũ — xem `../lab1-design.md` phần scaffold để biết bối cảnh phát hiện việc này).
- `spring-boot-maven-plugin`: cho phép `./mvnw spring-boot:run` (chạy trực tiếp không cần build jar trước) và đóng gói jar thành "fat jar" (chứa toàn bộ dependency bên trong) khi `package` — nếu không có plugin này, `./mvnw package` chỉ ra 1 jar rất nhỏ, thiếu hết dependency, chạy `java -jar` sẽ báo lỗi `ClassNotFoundException`.
