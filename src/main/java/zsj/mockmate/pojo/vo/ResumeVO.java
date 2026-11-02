package zsj.mockmate.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 简历列表里的一行：只给前端要展示的字段，不包含 rawText / profileJson */
@Data
public class ResumeVO {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    /** PENDING / PARSING / READY / FAILED —— 前端用来显示"解析中 / 就绪 / 失败" */
    private String parseStatus;
    private LocalDateTime createdAt;
}
