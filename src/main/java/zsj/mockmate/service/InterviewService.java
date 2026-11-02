package zsj.mockmate.service;

import zsj.mockmate.pojo.vo.InterviewMessageVO;
import zsj.mockmate.pojo.vo.InterviewReplyVO;
import zsj.mockmate.pojo.vo.InterviewSessionVO;

import java.util.List;

/**
 * 面试会话的业务层。
 * 会话怎么找、记忆怎么带、消息怎么落库，都在这里，控制器不碰。
 */
public interface InterviewService {

    /**
     * 一轮对话：没给会话就新开一场，给了就接着聊
     *
     * @param question       这一轮用户说的话
     * @param conversationId 会话号，可空；空或认不出都会新建
     * @param resumeId       只在<b>新建会话</b>时生效：这场面试依据哪份简历。可空
     * @return 会话号 + 模型回答
     */
    InterviewReplyVO chat(String question, String conversationId, Long resumeId);

    /** 当前登录用户的全部会话，新的在前 */
    List<InterviewSessionVO> listSessions();

    /** 删除一场会话（连同它的消息）。不是自己的会拒绝 */
    void deleteSession(Long sessionId);

    /** 一场会话的全部历史消息，时间正序。不是自己的会拒绝 */
    List<InterviewMessageVO> history(Long sessionId);
}
