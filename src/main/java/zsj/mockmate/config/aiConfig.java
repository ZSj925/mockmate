package zsj.mockmate.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置：全项目唯一的 ChatClient
 */
@Configuration
public class AiConfig {

    /** 面试官人设，来自配置文件 */
    private final String systemPrompt;

    public AiConfig(@Value("${app.ai.system-prompt}")String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        ChatMemory memory = MessageWindowChatMemory.builder()//设置全局上下文
                .maxMessages(20)          // 只保留最近 20 条
                .build();

        return builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    /**
     * 上下文配置（每次使用时调用）
     * @param chatMemory
     * @return
     */
    @Bean
    public MessageChatMemoryAdvisor chatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }
}
