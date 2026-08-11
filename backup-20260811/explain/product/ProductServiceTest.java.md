# ProductServiceTest.java

`src/test/java/com/example/redis_lab_prj/product/ProductServiceTest.java`

## Vai trò

Unit test cho `ProductService` — kiểm tra business logic tách biệt hoàn toàn khỏi DB thật.

## Giải thích

- `@ExtendWith(MockitoExtension.class)` + `@Mock private ProductRepository productRepository`: tạo một `ProductRepository` giả (mock), không chạm gì vào DB. `productService = new ProductService(productRepository)` ở `@BeforeEach` — nhờ `ProductService` dùng **constructor injection** (xem `product/ProductService.java.md`), test tự `new` được instance mà không cần khởi động Spring context, nên chạy cực nhanh (mili-giây, so với `@SpringBootTest` mất vài giây).
- `when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0))`: giả lập hành vi thật của JPA save — trả về chính object được truyền vào (JPA `save()` thường trả về entity đã persist, ở đây trả nguyên object nhận vào vì không cần giả lập việc sinh `id`).
- `ArgumentCaptor<Product>`: dùng khi cần **kiểm tra nội dung** của object được truyền vào `save()`, thay vì chỉ biết `save()` có được gọi hay không — quan trọng ở `create_savesProductWithGivenFields` để xác nhận field map đúng từ `ProductRequest` sang `Product`.

## Các case được cover

- Happy path: `create`, `findById` (tìm thấy), `update`.
- Edge case: `findById`/`delete` khi không tìm thấy → phải ném `ResourceNotFoundException`, và **không** được gọi `productRepository.delete(...)` (`verify(productRepository, never()).delete(any())`) — nếu thiếu dòng verify này, test vẫn pass dù code lỡ xoá nhầm trước khi kiểm tra tồn tại.

## Chưa cover

Concurrent case — vì `ProductService` (CRUD Product thuần) không có thao tác read-modify-write nào cần bảo vệ. Khác với `OrderService`, nơi bắt buộc phải nghĩ tới race condition (xem `order/OrderService.java.md` và `order/OrderServiceTest.java.md`).
