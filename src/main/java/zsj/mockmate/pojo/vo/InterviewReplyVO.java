package zsj.mockmate.pojo.vo;

/**
 * 一轮对话的返回：会话号 + 模型回答
 *
 * @param conversationId 这场面试的会话号
 * @param answer         模型这一轮的回答
 */
public record InterviewReplyVO(String conversationId, String answer) {}
