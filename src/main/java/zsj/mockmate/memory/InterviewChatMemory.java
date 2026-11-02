package zsj.mockmate.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zsj.mockmate.mapper.InterviewMapper;
import zsj.mockmate.pojo.ApiException;
import zsj.mockmate.pojo.entity.InterviewMessageEntity;
import zsj.mockmate.pojo.entity.InterviewSessionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 把 Spring AI 的对话记忆落到 interview_message 表。
 *
 * 为什么不用 MessageWindowChatMemory + 官方 JdbcChatMemoryRepository：
 * 那个组合每次 saveAll 传的是"裁剪后的整个窗口"，仓库是"先删后插"，
 * 库里每场只剩最近 20 条，历史回放和面试报告就没素材了。
 * 所以这里：add 只追加、get 只取最近 N 条，库里的全量不受影响。
 */
@Component
@Slf4j
public class InterviewChatMemory implements ChatMemory {

    private final InterviewMapper interviewMapper;

    /** 交给模型的条数上限，一问一答算 2 条 */
    private final int window;

    public InterviewChatMemory(InterviewMapper interviewMapper,
                               @Value("${app.ai.memory.window:12}") int window) {
        this.interviewMapper = interviewMapper;
        this.window = window;
    }

    @Override
    public void add(String conversationId, Message message) {
        add(conversationId, List.of(message));
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Long sessionId = parse(conversationId);
        if (sessionId == null || messages == null || messages.isEmpty()) return;

        int round = interviewMapper.countByRole(sessionId, "INTERVIEWER") + 1;
        for (Message message : messages) {
            // 官方叫 USER/ASSISTANT，面试表里叫 CANDIDATE/INTERVIEWER
            String role = switch (message.getMessageType()) {
                case USER -> "CANDIDATE";
                case ASSISTANT -> "INTERVIEWER";
                default -> null;                   // SYSTEM / TOOL 不落库
            };
            if (role == null) continue;

            InterviewMessageEntity row = new InterviewMessageEntity();
            row.setSessionId(sessionId);
            row.setRoundNo(round);
            row.setRole(role);
            row.setContent(message.getText());
            interviewMapper.insertMessage(row);

            if ("INTERVIEWER".equals(role)) round++;
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        Long sessionId = parse(conversationId);
        if (sessionId == null) return List.of();

        List<InterviewMessageEntity> recent = interviewMapper.findRecentMessages(sessionId, window);
        List<Message> messages = new ArrayList<>(recent.size());
        // SQL 倒序取的，翻回时间正序，模型很在意顺序
        for (int i = recent.size() - 1; i >= 0; i--) {
            InterviewMessageEntity row = recent.get(i);
            messages.add("CANDIDATE".equals(row.getRole())
                    ? new UserMessage(row.getContent())
                    : new AssistantMessage(row.getContent()));
        }
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        // 不删！库里的对话记录要留作回放/报告。真需要删再单独开接口
    }

    private Long parse(String conversationId) {
        try {
            return conversationId == null ? null : Long.valueOf(conversationId);
        } catch (NumberFormatException e) {
            return null;
        }
    }


}