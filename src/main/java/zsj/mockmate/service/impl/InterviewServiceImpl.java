package zsj.mockmate.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zsj.mockmate.mapper.InterviewMapper;
import zsj.mockmate.pojo.ApiException;
import zsj.mockmate.pojo.entity.InterviewSessionEntity;
import zsj.mockmate.pojo.vo.InterviewMessageVO;
import zsj.mockmate.pojo.vo.InterviewReplyVO;
import zsj.mockmate.pojo.vo.InterviewSessionVO;
import zsj.mockmate.service.InterviewService;

import java.util.List;
import java.util.Objects;

/**
 * 面试会话的业务实现。
 *
 * 会话号 = interview_session 的主键 id，同时也是 chat memory 的 conversationId：
 * 记忆按会话分区，写库也按会话分区，两件事共用同一个号。
 */
@Slf4j
@Service
public class InterviewServiceImpl implements InterviewService {

    private static final String ROLE_INTERVIEWER = "INTERVIEWER";

    private final ChatClient chatClient;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final InterviewMapper interviewMapper;

    public InterviewServiceImpl(ChatClient chatClient,
                                MessageChatMemoryAdvisor chatMemoryAdvisor,
                                InterviewMapper interviewMapper) {
        this.chatClient = chatClient;
        this.chatMemoryAdvisor = chatMemoryAdvisor;
        this.interviewMapper = interviewMapper;
    }

    /**
     * 对话的业务层
     * @param question       这一轮用户说的话
     * @param conversationId 会话号，可空；空或认不出都会新建
     * @return 会话号 + 模型回答
     */
    @Override
    public InterviewReplyVO chat(String question, String conversationId) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 决定这一轮属于哪场会话（没有就新建一场）
        Long sessionId = resolveSession(conversationId, userId);

        // 2. 调模型。把会话号交给记忆 advisor：它读上文、写回答，都按这个号分区
        String answer = chatClient.prompt()
                .user(question)
                .advisors(chatMemoryAdvisor)                                //挂上记事本
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(sessionId)))  //告诉它翻哪本
                .call()
                .content();

        // 3. 会话聊到第几轮了，同步回 interview_session（INTERVIEWER 消息数 = 已问几题）
        interviewMapper.touchRound(sessionId, interviewMapper.countByRole(sessionId, ROLE_INTERVIEWER));

        return new InterviewReplyVO(String.valueOf(sessionId), answer);
    }

    @Override
    public List<InterviewSessionVO> listSessions() {
        return interviewMapper.findSessionsByUser(StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        long userId = StpUtil.getLoginIdAsLong();

        // 先删会话本身：SQL 的 where 里带 user_id，不是自己的会返回 0 行，天然防越权
        int removed = interviewMapper.deleteSession(sessionId, userId);
        if (removed == 0) {
            throw new ApiException("会话不存在或无权限删除");
        }
        // 再删消息。顺序不能反：上面没删成功时绝不能动消息
        interviewMapper.deleteMessages(sessionId);
        log.info("删除会话 {}，用户 {}", sessionId, userId);
    }

    @Override
    public List<InterviewMessageVO> history(Long sessionId) {
        InterviewSessionEntity session = interviewMapper.getSession(sessionId);
        if (session == null) {
            throw new ApiException("会话不存在");
        }
        if (!Objects.equals(session.getUserId(), StpUtil.getLoginIdAsLong())) {
            throw new ApiException("无权访问这场会话");
        }

        return interviewMapper.findMessages(sessionId).stream().map(row -> {
            InterviewMessageVO vo = new InterviewMessageVO();
            vo.setRole(row.getRole());
            vo.setContent(row.getContent());
            vo.setRoundNo(row.getRoundNo());
            vo.setCreatedAt(row.getCreatedAt());
            return vo;
        }).toList();
    }

    /**
     * 没给 id / 认不出 / 不是自己的 → 开新会话；否则沿用。
     * 注意：别人的会话不会报错，而是静默给一个新的，避免把别人的内容喂给模型。
     */
    private Long resolveSession(String cid, long userId) {
        if (cid != null && !cid.isBlank()) {
            try {
                Long id = Long.valueOf(cid);
                InterviewSessionEntity session = interviewMapper.getSession(id);

                if (session == null) {
                    log.info("会话 {} 不存在，改为新建会话", id);
                } else if (Objects.equals(session.getUserId(), userId)) {
                    return id;
                } else {
                    log.warn("会话 {} 不属于用户 {}，改为新建会话", id, userId);
                }
            } catch (NumberFormatException e) {
                // 老版本浏览器里存的是 UUID，认不出就当没有
                log.info("conversationId={} 不是数字（旧版 UUID），改为新建会话", cid);
            }
        }

        InterviewSessionEntity session = new InterviewSessionEntity();
        session.setUserId(userId);
        session.setJobType("JAVA_BACKEND");
        session.setStatus("ONGOING");
        session.setQuestionCount(5);
        session.setCurrentRound(0);
        interviewMapper.insertSession(session);

        // useGeneratedKeys 没生效时这里会是 null，记忆会全落到 "null" 这一个坑里，必须挡住
        if (session.getId() == null) {
            throw new ApiException("会话创建失败：没有拿到自增 id");
        }

        log.info("新建会话 {}，用户 {}", session.getId(), userId);
        return session.getId();
    }
}
