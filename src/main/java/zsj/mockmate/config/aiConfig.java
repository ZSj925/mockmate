package zsj.mockmate.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置：全项目唯一的 ChatClient
 *
 * <p><b>注意：这里不要挂 defaultAdvisors。</b>
 *
 * <p>原因有两个：
 * <ol>
 *   <li>挂了 defaultAdvisors 之后，<b>每一次</b> chatClient 调用都会走记忆 advisor，
 *       没传 conversationId 的调用会直接抛
 *       {@code IllegalArgumentException: conversationId cannot be null}。
 *       比如简历摘要生成、以后的简历结构化，都属于"不需要记忆"的调用。</li>
 *   <li>记忆只应该有一套。真正落地到 interview_message 的是
 *       {@link zsj.mockmate.memory.InterviewChatMemory}，由 InterviewServiceImpl 在每次对话时
 *       显式 .advisors(chatMemoryAdvisor) 挂上去。如果这里再挂一个内存版
 *       MessageWindowChatMemory，两套记忆会同时往 prompt 里塞历史，token 白烧。</li>
 * </ol>
 */
@Configuration
public class AiConfig {

    /** 面试官人设，来自配置文件 */
    private final String systemPrompt;

    public AiConfig(@Value("${app.ai.system-prompt}") String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(systemPrompt)   // 只设人设，不挂 advisor
                .build();
    }

    /**
     * 记忆 advisor：由调用方按需挂载（见 InterviewServiceImpl.chat）。
     *
     * <p>它注入的 ChatMemory 是 {@link zsj.mockmate.memory.InterviewChatMemory}
     * （Spring AI 的内存版因为它标了 @ConditionalOnMissingBean 会自动让位）。
     */
    @Bean
    public MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
