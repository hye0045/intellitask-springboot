package com.minhhue.IntelliTask.service;

import com.minhhue.IntelliTask.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    // THAY ĐỔI 1: Đọc key mới từ file properties
    @Value("${groq.api.key}")
    private String groqApiKey;

    // THAY ĐỔI 2: URL của API Groq
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private static final String MODEL = "openai/gpt-oss-120b";

    private final RestTemplate restTemplate = new RestTemplate();


    public String extractTasksFromText(String text,List<User> members) {
        HttpHeaders headers = new HttpHeaders();
        // Sử dụng key mới
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder memberNames = new StringBuilder("Danh sách thành viên và skills: \n");
        for (User member : members) {
        memberNames.append("- Tên: ").append(member.getFullName())
                   .append(", ID: ").append(member.getId())
                   .append(", Kỹ năng: ").append(member.getSkills()).append("\n");
        }

        String systemPrompt = """
            Bạn là Project Manager AI cực kỳ xuất sắc, cực kỳ chính xác và luôn tuân thủ nghiêm ngặt format.
            Danh sách thành viên đội (ID + kỹ năng chính):
            {memberNames.toString()}
            Nhiệm vụ của bạn:
            Từ đoạn mô tả dự án dưới đây, hãy trích xuất TẤT CẢ các công việc riêng biệt cần làm.
            Với từng công việc, hãy suy nghĩ từng bước theo đúng thứ tự sau (bạn phải nghĩ trong <reasoning>...</reasoning>, không được bỏ qua bước nào):
            1. Xác định title ngắn gọn, chuyên nghiệp (tối đa 8 từ).
            2. Viết description ngắn (1–2 câu) tóm tắt công việc.
            3. Tìm due date/deadline nếu có trong text (định dạng chính xác YYYY-MM-DD). Nếu không có hoặc không rõ → để null.
            4. Phân tích kỹ năng cần thiết cho công việc này.
            5. So sánh với kỹ năng của từng thành viên → chọn đúng 1 người PHÙ HỢP NHẤT (không chọn random, không chọn nhiều người).
            6. Nếu không có ai phù hợp → để suggestedAssigneeId là null.
            Input dự án:
            \"\"\"{user_input}\"\"\"

            Chỉ trả về một mảng JSON hợp lệ, không thêm bất kỳ text nào khác, kể cả giải thích hay markdown:

            [{"title": "...", "description": "...", "dueDate": "YYYY-MM-DD" hoặc null, "suggestedAssigneeId": ID hoặc null}, ...]

            Nếu không tìm thấy công việc nào → trả về [] (mảng rỗng).
            """ 
            + memberNames.toString();
        
        Map<String, Object> requestBody = Map.of(
            "model", MODEL,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", text)
            ),
            "temperature", 0.5
        );
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Gọi đến URL mới
        Map<String, Object> response = restTemplate.postForObject(GROQ_API_URL, requestEntity, Map.class);

        // Phần xử lý response giữ nguyên
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                if (firstChoice.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        }

        return "[]";
    }

    public String generateTaskDescription(String taskTitle) {
        //prompt để tạo description chi tiết cho task
                String systemPrompt = """ 
        Bạn là Senior Project Manager với 15 năm kinh nghiệm tại Big Tech.
        Hãy viết một task description cực kỳ chuyên nghiệp, rõ ràng, có tính hành động cao cho task có tiêu đề:
        "{taskTitle}"
        Bắt buộc tuân thủ chính xác cấu trúc sau (không thêm phần nào khác, không dùng markdown bold ngoài tiêu đề phần):
        Mục tiêu:
        [Viết 2-3 câu mô tả mục tiêu cuối cùng của task, tập trung vào kết quả kinh doanh/giá trị mang lại]
        Các đầu việc chính:
        - [Công việc con 1 – bắt đầu bằng động từ mạnh: Thiết kế, Phát triển, Triển khai, Nghiên cứu...]
        - [Công việc con 2]
        - [Công việc con 3]
        - [Công việc con 4, nếu cần]
        Yêu cầu/Lưu ý:
        - [Yêu cầu kỹ thuật, chất lượng, tiêu chuẩn cụ thể]
        - [Lưu ý về rủi ro, dependency, best practices]
        - [Tiêu chí nghiệm thu (Done khi...)]
        - [Nếu có] Tài liệu tham khảo hoặc công cụ bắt buộc sử dụng

        Viết bằng tiếng Việt, giọng chuyên nghiệp, ngắn gọn nhưng đầy đủ, mỗi gạch đầu dòng tối đa 15 từ.""";
                
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = Map.of(
            "model", MODEL,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", taskTitle)
            ),
            "temperature", 0.5
        );
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        Map<String, Object> response = restTemplate.postForObject(GROQ_API_URL, requestEntity, Map.class);

        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                if (firstChoice.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        }

        return "";
    }
    public String summarizeConversation(String conversationHistory) {
    String systemPrompt = """
        Bạn là Meeting Summarizer AI chuyên nghiệp nhất thế giới, đã xử lý hàng triệu cuộc họp tại Google, Microsoft, Amazon.
        Nhiệm vụ duy nhất của bạn là tóm tắt hội thoại team một cách cực kỳ chính xác, ngắn gọn và có cấu trúc cố định.

        Yêu cầu bắt buộc:
        - Đọc toàn bộ hội thoại trước khi tóm tắt.
        - Phân biệt rõ ai nói gì (nếu cần thì ghi tên người đề xuất).
        - Không thêm thắt ý kiến cá nhân, không suy diễn.
        - Chỉ ghi những gì thực sự được nói ra.

        Output phải theo đúng cấu trúc sau, không thêm bất kỳ từ nào ngoài cấu trúc này:

        **Tóm tắt cuộc họp:**

        **Chủ đề chính:** [1-2 câu tóm tắt mục đích cuộc thảo luận]

        **Ý chính đã thống nhất:**
        • [Ý 1]
        • [Ý 2]
        • ...

        **Đề xuất đã được đưa ra:**
        • [Đề xuất] – [Tên người đề xuất] → [Phản hồi của team: đồng ý/đang cân nhắc/từ chối/không ai trả lời]

        **Vấn đề còn tồn đọng / Action items:**
        • [Vấn đề/Action] – [Người chịu trách nhiệm nếu có] – [Deadline nếu có]

        **Quyết định cuối cùng (nếu có):**
        • [Quyết định 1]
        • [Quyết định 2]
        • Không có quyết định chính thức (nếu thực sự không có)

        **Người tham gia:** [Danh sách tên hoặc nickname xuất hiện trong hội thoại, cách nhau dấu phẩy]

        """;

        String userMessage = "Đoạn hội thoại:\\n" + conversationHistory;
    HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = Map.of(
            "model", MODEL,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "temperature", 0.5
        );
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        Map<String, Object> response = restTemplate.postForObject(GROQ_API_URL, requestEntity, Map.class);

        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                if (firstChoice.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }
        }

        return "";
    }
}