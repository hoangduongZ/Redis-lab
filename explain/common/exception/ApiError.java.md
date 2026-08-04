# ApiError.java

`src/main/java/com/example/redis_lab_prj/common/exception/ApiError.java`

## Vai trò

Định dạng JSON thống nhất cho **mọi** response lỗi trả về từ API — client luôn nhận được đúng 1 shape: `timestamp`, `status`, `error`, `message`, bất kể lỗi là gì.

## Giải thích

Nếu không có class này, mỗi loại lỗi có thể trả JSON khác hình dạng (có chỗ trả `{"error": "..."}`, chỗ khác trả `{"message": "..."}`) — client (frontend, hoặc test) phải viết logic riêng để đọc lỗi từ từng endpoint. Một shape thống nhất giúp xử lý lỗi ở phía client chỉ cần viết một lần.

Chỉ có getter, không setter — object này luôn được tạo mới hoàn chỉnh ngay tại chỗ khởi tạo (`GlobalExceptionHandler.build()`), không có lý do gì để sửa lại sau khi đã tạo.
