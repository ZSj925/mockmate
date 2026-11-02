package zsj.mockmate.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史回放里的一条消息（前端切换会话时用来重绘气泡）。
 * role: INTERVIEWER（AI 说的）/ CANDIDATE（候选人说的）
 */
@Data
public class InterviewMessageVO {

    private String role;
    private String content;
    private Integer roundNo;
    private LocalDateTime createdAt;
}
