package zsj.mockmate.pojo.vo;

/**
 * 上传结果：前端用它显示"识别到多少字"
 */
public record ResumeUploadVO(Long resumeId,
                             String fileName,
                             Long fileSize,
                             Integer charCount) {
}
