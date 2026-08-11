# ProductControllerTest.java

`src/test/java/com/example/redis_lab_prj/product/ProductControllerTest.java`

## Vai trò

Test tầng HTTP của `ProductController` — kiểm tra request/response JSON, status code, validation — **không** chạm tới `ProductService` thật (đã mock).

## Giải thích

- `@WebMvcTest(ProductController.class)`: chỉ khởi động phần liên quan tới MVC (`DispatcherServlet`, `@RestControllerAdvice`, Jackson converter...) cho đúng 1 Controller được chỉ định — không load `@Service`, `@Repository`, DataSource... Nhẹ hơn `@SpringBootTest` nhiều, nhưng vẫn nặng hơn unit test thuần vì có khởi động một phần context thật.
- `@MockitoBean private ProductService productService`: thay bean `ProductService` thật trong context bằng mock — đây là lý do test này không cần DB, kể cả H2.
- `MockMvc`: giả lập HTTP request **không qua network thật** (không có cổng, không có Tomcat) — nhanh hơn nhiều so với gọi `TestRestTemplate` qua HTTP thật.
- `jsonPath("$.name").value("Keyboard")`: kiểm tra đúng field trong JSON response, thay vì so sánh cả chuỗi JSON (dễ vỡ khi thêm field mới không liên quan tới điều đang test).

## Các case được cover

- Happy path: `create` trả 201 với body đúng, `findAll` trả danh sách.
- Failure case: `create` với `name` rỗng → 400 (kiểm tra `@Valid`/`@NotBlank` ở `ProductRequest` thật sự hoạt động, không phải giả định).
- Edge case: `findById` khi Service ném `ResourceNotFoundException` → 404 (kiểm tra `GlobalExceptionHandler` thật sự bắt được exception từ Controller).

## Vì sao vẫn cần test này dù đã có `ProductServiceTest`

`ProductServiceTest` không biết gì về HTTP status code, JSON shape, hay việc `@Valid` có thực sự chặn request sai không — hai lớp test kiểm tra hai tầng khác nhau (business logic vs. hợp đồng API).
