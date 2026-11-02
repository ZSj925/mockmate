package zsj.mockmate.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 简历列表里的一行：只给前端要展示的字段，不包含 rawText */
@Data
public class ResumeVO {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime createdAt;
}