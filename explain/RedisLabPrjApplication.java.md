# RedisLabPrjApplication.java

`src/main/java/com/example/redis_lab_prj/RedisLabPrjApplication.java`

## Vai trò

Entry point của app — class có `main()` để chạy Spring Boot.

## Giải thích

`@SpringBootApplication` là annotation gộp của 3 annotation:

- `@Configuration` — class này có thể khai báo bean.
- `@EnableAutoConfiguration` — Spring Boot tự cấu hình (DataSource, JPA, MVC dispatcher...) dựa vào dependency có trong `pom.xml`. Đây là lý do chỉ cần thêm `spring-boot-starter-data-jpa` là JPA đã hoạt động, không cần tự cấu hình `EntityManagerFactory` bằng tay.
- `@ComponentScan` — quét toàn bộ package con của `com.example.redis_lab_prj` để tìm `@Component`, `@Service`, `@Repository`, `@RestController`. Đây là lý do `ProductController`, `OrderService`... không cần khai báo thủ công ở đâu cả — chỉ cần nằm đúng package con.

`SpringApplication.run(...)` khởi tạo ApplicationContext (container chứa toàn bộ bean), rồi start embedded Tomcat (vì có `spring-boot-starter-web`).

## Vì sao class này nằm ở package gốc

`@ComponentScan` mặc định quét từ package chứa class có `@SpringBootApplication` trở xuống. Nếu class này bị di chuyển vào package con (vd. `com.example.redis_lab_prj.app`), các bean ở package ngang hàng khác sẽ không được quét — đây là lỗi runtime rất phổ biến khi mới học Spring Boot.
