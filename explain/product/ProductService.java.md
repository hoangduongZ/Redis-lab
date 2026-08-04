# ProductService.java

`src/main/java/com/example/redis_lab_prj/product/ProductService.java`

## Vai trò

Chứa business logic của Product — lớp trung gian giữa Controller (HTTP) và Repository (DB). Controller không được gọi thẳng Repository để giữ business rule tập trung một chỗ, không rải rác.

## Giải thích

- `@Service`: đánh dấu để `@ComponentScan` tạo bean, và về mặt ý nghĩa là "đây là tầng nghiệp vụ", khác với `@Repository` (tầng data) hay `@RestController` (tầng HTTP).
- Constructor injection (`public ProductService(ProductRepository productRepository)`): Spring tự inject `ProductRepository` bean vào khi tạo `ProductService`. Không dùng `@Autowired` trên field vì constructor injection cho phép field là `final`, và test có thể `new ProductService(mockRepo)` trực tiếp mà không cần khởi động Spring context (xem `ProductServiceTest`).
- `findById`: ném `ResourceNotFoundException` nếu không tìm thấy, thay vì trả về `null`. Việc này đẩy trách nhiệm xử lý "not found" ra khỏi Service, tập trung ở `GlobalExceptionHandler` — Controller/Service không cần tự viết `if (product == null) return 404` ở từng chỗ gọi.
- `update`: load entity cũ ra rồi set field mới rồi save — không tạo entity mới. Nhờ vậy Hibernate hiểu đây là update (entity đã có id), không phải insert.

## Điều chưa làm ở đây

Không có cache-aside, không TTL — vì đây là Phase 1, Redis chưa vào cuộc. `findAll()`/`findById()` hiện tại luôn query thẳng Postgres.
