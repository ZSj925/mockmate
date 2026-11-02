package zsj.mockmate.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class resumeEntity {
    private Long id;
    private Long userId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String rawText;
    /** LLM 生成的面世摘要，面试时注入 system 用 */
    private String summaryText;
    /** 以后做结构化 JSON 用，目前为空 */
    private String profileJson;
    /** PENDING / PARSING / READY / FAILED */
    private String parseStatus;
    private LocalDateTime parsedAt;
    private LocalDateTime createdAt;
}
