# Giáo Án Redis Phong Cách Feynman - Phần 2: Hình Dáng Của Dữ Liệu (Vượt ra ngoài Caching)

**Người hướng dẫn:** "Thầy" Richard Feynman
**Mục tiêu:** Hiểu rằng Redis không chỉ lưu chuỗi văn bản vô tri, mà nó cung cấp các "công cụ" được thiết kế đặc biệt để giải quyết những bài toán hóc búa nhất mà Database truyền thống phải đầu hàng.

---

## 1. Mở Đầu: Chiếc Bảng Trắng Không Chỉ Viết Chữ

Ở Phần 1, chúng ta dùng Redis như một chiếc bảng trắng chỉ để viết những dòng chữ đơn giản: *"Harry Potter = Nội dung XYZ"*. Chúng ta gọi đó là kiểu dữ liệu **String** (Chuỗi). 

Nhưng nếu tôi yêu cầu bạn quản lý một hàng người đang xếp hàng chờ mua vé thì sao? Hoặc quản lý một bảng điểm thi đấu thể thao luôn thay đổi từng giây? Bạn không thể cứ viết rồi xóa liên tục một chuỗi văn bản dài ngoằng trên bảng được. Nó rất lộn xộn và chậm chạp.

Đó là lúc Redis đưa cho bạn những **Hình dáng dữ liệu (Data Structures)** mới. Hôm nay, chúng ta sẽ tập trung vào hình dáng quyền lực nhất của Redis: **Sorted Sets** (Tập hợp được sắp xếp).

---

## 2. Trải nghiệm Nỗi Đau: Bài Toán Bảng Xếp Hạng (Leaderboard)

Giả sử bạn đang làm game "Flappy Bird". Game có 1 triệu người chơi. Sếp yêu cầu bạn làm một API để lấy ra **Top 10 người điểm cao nhất hiện tại**.

### 💻 Nỗi đau của MySQL (Database truyền thống):
Nếu bạn lưu điểm trong SQL, mỗi khi có người gọi API lấy Top 10, database của bạn sẽ phải làm gì?
Nó phải gom đủ 1 triệu dòng dữ liệu lại, mang ra so sánh, **SẮP XẾP TỪNG DÒNG (ORDER BY score DESC)**, rồi cắt lấy 10 người đứng đầu. 

```sql
SELECT player_name, score FROM leader_board ORDER BY score DESC LIMIT 10;
```

Hành động SẮP XẾP (Sort) là một trong những việc mệt mỏi và tốn tài nguyên nhất của máy tính. Cứ có 1000 người vào xem bảng xếp hạng cùng lúc, MySQL của bạn sẽ phải làm cái việc nặng nhọc đó 1000 lần. Server lại bốc khói!

---

## 3. Thuốc Giải: Tấm Bảng Gỗ Có Khe Trượt (Sorted Sets)

Thay vì ném hàng triệu phiếu điểm vào một cái rổ (SQL) rồi mỗi lần cần lại lôi ra đếm và sắp xếp... Tại sao chúng ta không thiết kế một cái bảng đặc biệt?

Hãy tưởng tượng một tấm bảng gỗ có các khe trượt. Mỗi khi người chơi được cộng điểm, bạn chỉ cần cầm thẻ tên của họ, trượt nhẹ lên vị trí mới tương ứng với số điểm. 

**Tấm bảng này LUÔN LUÔN Ở TRẠNG THÁI ĐÃ ĐƯỢC SẮP XẾP.** 
Khi ai đó hỏi "Cho tôi Top 10", bạn không cần sắp xếp gì cả! Bạn chỉ việc nhìn lên trên cùng của tấm bảng, bốc đúng 10 cái tên đầu tiên ra. Tốc độ là ngay lập tức (O(log(N))).

Đó chính là cơ chế của **Redis Sorted Sets (ZSet)**.

---

## 4. Thực Hành Spring Boot: Xây dựng Bảng Xếp Hạng

Không dùng `@Cacheable` nữa, lần này chúng ta sẽ dùng thẳng công cụ `RedisTemplate` của Spring Boot để "trượt thẻ trên bảng gỗ".

### 💻 Code Java + ZSet:

```java
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;

    public LeaderboardService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 1. Hành động: Cập nhật điểm của người chơi
    public void addScore(String playerName, double score) {
        // ZSet có một phép màu: Nếu playerName đã có, nó tự cộng/trừ điểm và tự trượt lên/xuống.
        redisTemplate.opsForZSet().add("game_leaderboard", playerName, score);
        System.out.println("Đã cập nhật điểm cho " + playerName);
    }

    // 2. Hành động: Lấy Top 10 người điểm cao nhất (Gần như không tốn thời gian)
    public Set<ZSetOperations.TypedTuple<String>> getTop10() {
        // reverseRangeWithScores: Lấy từ cao xuống thấp, lấy từ vị trí 0 đến vị trí 9
        Set<ZSetOperations.TypedTuple<String>> top10 = 
            redisTemplate.opsForZSet().reverseRangeWithScores("game_leaderboard", 0, 9);
        
        System.out.println("⚡ Đã lấy xong Top 10 mà không cần Sort!");
        return top10;
    }
}
```

Bạn thấy sức mạnh của hình dáng dữ liệu chưa? Bài toán làm gục ngã MySQL giờ đây được giải quyết chỉ bằng một lệnh gọi hàm đơn giản trong Redis.

---

## 5. Những Hình Dáng Khác Của Redis (Lướt nhanh)

Bên cạnh ZSet (Bảng xếp hạng), Redis còn có:
*   **Lists (Danh sách):** Giống như một cái ống nước. Bạn nhét đồ vào một đầu, rút đồ ra ở đầu kia. Tuyệt vời để làm **Message Queue** (Hàng đợi công việc - ví dụ: Xếp hàng gửi email dần dần).
*   **Sets (Tập hợp):** Giống như một cái rổ kỳ diệu: Bạn ném bao nhiêu quả bóng vào cũng được, nhưng nếu ném quả bóng trùng màu đã có, nó tự ném ra. Tuyệt vời để đếm **số lượng người dùng duy nhất truy cập web** mỗi ngày (không tính ai vào 2 lần).
*   **Hashes (Bảng băm):** Giống như một ngăn kéo nhỏ. Thay vì lưu cả một chuỗi JSON dài, bạn chia ra ngăn chứa "Tên", ngăn chứa "Tuổi". Muốn đổi tên thì chỉ mở ngăn "Tên" ra sửa, rất nhanh và tiết kiệm.

---

## 6. Tổng Kết & Bài Tập Tư Duy

Bài học này giúp bạn gỡ bỏ định kiến: "Redis chỉ để làm Cache". Sự thật là, nếu bạn biết sử dụng đúng Cấu trúc dữ liệu của Redis, nó có thể trở thành trái tim xử lý logic tốc độ cao cho ứng dụng của bạn.

**Bài tập tư duy cho phần 3:**
Giả sử bạn đang bán 1 chiếc vé concert duy nhất của BlackPink, và có **1000 người cùng bấm nút "Mua" trong cùng 1 mili-giây**. 
Mỗi người lại chạy trên 1 server Spring Boot khác nhau. Làm sao bạn đảm bảo chỉ đúng 1 người mua được vé mà không bị tình trạng "bán lố" (Over-selling)? 

*(Gợi ý: Redis chạy "Đơn luồng" (Single-threaded) - Giống như cửa hàng chỉ có đúng 1 cô thu ngân, nên mọi người bắt buộc phải xếp hàng từng người một. Hẹn gặp lại ở Phần 3: Distributed Lock).*
