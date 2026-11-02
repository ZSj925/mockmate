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
    private String profileJson;
    private LocalDateTime createdAt;
}
