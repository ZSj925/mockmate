package zsj.mockmate.pojo.vo;

/**
 * 上传结果：前端用它显示"识别到多少字"和"摘要还在生成中"
 *
 * @param resumeId    新简历的 id
 * @param fileName    清洗后的文件名
 * @param fileSize    字节数
 * @param charCount   抽出来的正文字数
 * @param parseStatus PENDING = 摘要后台生成中；READY = 已就绪
 */
public record ResumeUploadVO(Long resumeId,
                             String fileName,
                             Long fileSize,
                             Integer charCount,
                             String parseStatus) {
}
