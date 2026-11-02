package zsj.mockmate.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewMessageEntity {
    private Long id;
    private Long sessionId;
    private Integer roundNo;
    private String role;            // INTERVIEWER / CANDIDATE
    private String content;
    private LocalDateTime createdAt;
}