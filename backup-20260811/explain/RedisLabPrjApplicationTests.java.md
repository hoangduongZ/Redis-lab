# RedisLabPrjApplicationTests.java

`src/test/java/com/example/redis_lab_prj/RedisLabPrjApplicationTests.java`

## Vai trò

Test "khói" (smoke test) tối thiểu do Spring Initializr tự sinh — không test business logic gì cả, chỉ kiểm tra một điều duy nhất: **toàn bộ ApplicationContext khởi động được, không lỗi bean nào**.

## Giải thích

- `@SpringBootTest`: khởi động **toàn bộ** Spring context thật (không mock gì), giống hệt lúc chạy app thật — quét mọi `@Component`/`@Service`/`@Repository`/`@RestController`, kết nối DataSource, khởi tạo JPA EntityManagerFactory...
- `contextLoads()` để trống: bản thân việc annotation test này chạy được **không ném exception** đã là một assertion ngầm. Nếu bất kỳ bean nào thiếu dependency, cấu hình sai, hoặc không kết nối được DB, test này sẽ đỏ ngay cả khi không viết `assert` nào.

## Vì sao test này cần Postgres hay H2

`@SpringBootTest` đọc cấu hình thật từ `application.yml` — nhưng `src/test/resources/application.yml` (xem `resources-test/application.yml.md`) override lại `spring.datasource.*` để trỏ vào H2 in-memory thay vì Postgres thật. Nếu không có override này, chạy `./mvnw test` trên máy không có Postgres đang chạy sẽ làm test này (và mọi test dùng `@SpringBootTest`) đỏ ngay từ bước khởi động context — trước cả khi vào tới logic bên trong.
