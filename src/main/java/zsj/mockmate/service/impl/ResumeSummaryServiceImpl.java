package zsj.mockmate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import zsj.mockmate.config.AsyncConfig;
import zsj.mockmate.mapper.ResumeMapper;
import zsj.mockmate.pojo.entity.resumeEntity;
import zsj.mockmate.service.ResumeSummaryService;

/**
 * 简历摘要生成实现。
 *
 * <p>为什么是异步：本地 qwen3:8b 生成一份摘要要十几秒，放在上传请求里会让用户等到超时。
 * 上传接口入完库立刻返回，摘要交给 {@link AsyncConfig} 里的单线程池。
 */
@Slf4j
@Service
public class ResumeSummaryServiceImpl implements ResumeSummaryService {

    /**
     * 摘要提示词。
     *
     * <p>这几条硬性要求都是踩过坑加上的：
     * 只许用原文事实（防模型编造技术栈）、剔除隐私（防面试官问手机号）、
     * 保留原词（"无状态鉴权"改成"登录鉴权"就丢了追问的钩子）。
     */
    private static final String SUMMARY_PROMPT = """
            你是简历结构化助手。下面是一份中文简历原文，请压缩成 600~800 字的"面试提问素材清单"。

            输出结构（严格按此，不要输出任何额外说明）：
            【候选人】姓名｜求职意向｜学历（学校/专业/时间）
            【技能】按类别罗列，保留原文措辞
            【项目 N】项目名｜角色｜时间｜技术栈
              · 该项目的具体做法，每条一行，保留原文关键措辞
            【可追问】3~5 个能深挖的方向

            硬性要求：
            1. 项目名称、技术名词必须与原文完全一致，一个字都不改
            2. 只提取原文出现过的事实，禁止补充原文没有的技术、项目、成果
            3. 不输出手机号、邮箱、年龄、性别、政治面貌
            4. 不写"学习能力强""吃苦耐劳"这类无法追问的形容词
            5. 直接输出正文，不要用 ``` 代码块包裹
            """;

    private final ChatClient chatClient;
    private final ResumeMapper resumeMapper;

    public ResumeSummaryServiceImpl(ChatClient chatClient, ResumeMapper resumeMapper) {
        this.chatClient = chatClient;
        this.resumeMapper = resumeMapper;
    }

    @Override
    @Async(AsyncConfig.RESUME_EXECUTOR)
    public void generateAsync(Long resumeId) {
        try {
            generate(resumeId);
        } catch (Exception e) {
            // @Async 方法里抛出的异常会被吞掉，必须自己兜住，否则状态永远停在 PARSING
            log.error("简历 {} 摘要生成失败", resumeId, e);
            markFailed(resumeId);
        }
    }

    @Override
    public String generate(Long resumeId) {
        resumeEntity resume = resumeMapper.findById(resumeId);
        if (resume == null) {
            log.warn("简历 {} 不存在，跳过摘要生成", resumeId);
            return null;
        }
        // 幂等：已经生成过就不再花钱
        if (STATUS_READY.equals(resume.getParseStatus()) && StringUtils.hasText(resume.getSummaryText())) {
            return resume.getSummaryText();
        }

        String rawText = resume.getRawText();
        if (!StringUtils.hasText(rawText)) {
            markFailed(resumeId);
            return null;
        }

        resumeMapper.updateStatus(resumeId, STATUS_PARSING);
        log.info("开始生成简历 {} 的摘要，原文 {} 字", resumeId, rawText.length());

        String summary = chatClient.prompt()
                // .system() 是替换 defaultSystem，这里正好把"面试官人设"换成"摘要助手"
                .system(SUMMARY_PROMPT)
                .user(rawText)
                .call()
                .content();

        summary = stripCodeFence(summary);
        if (!StringUtils.hasText(summary)) {
            markFailed(resumeId);
            return null;
        }

        resumeMapper.updateSummary(resumeId, summary, STATUS_READY);
        log.info("简历 {} 摘要生成完成，{} 字", resumeId, summary.length());
        return summary;
    }

    @Override
    public String getSummary(Long resumeId) {
        if (resumeId == null) {
            return null;
        }
        resumeEntity resume = resumeMapper.findById(resumeId);
        if (resume == null || !StringUtils.hasText(resume.getSummaryText())) {
            return null;
        }
        return resume.getSummaryText();
    }

    @Override
    public boolean needGenerate(Long resumeId) {
        if (resumeId == null) {
            return false;
        }
        resumeEntity resume = resumeMapper.findById(resumeId);
        if (resume == null) {
            return false;
        }
        if (StringUtils.hasText(resume.getSummaryText())) {
            return false;
        }
        return !STATUS_PARSING.equals(resume.getParseStatus());
    }

    private void markFailed(Long resumeId) {
        try {
            resumeMapper.updateStatus(resumeId, STATUS_FAILED);
        } catch (Exception e) {
            log.warn("简历 {} 状态置为 FAILED 也失败了", resumeId, e);
        }
    }

    /** 模型经常把输出包在 ``` 里，剥掉 */
    private String stripCodeFence(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String t = text.trim();
        if (t.startsWith("```")) {
            int firstLineEnd = t.indexOf('\n');
            if (firstLineEnd > 0) {
                t = t.substring(firstLineEnd + 1);
            }
            int lastFence = t.lastIndexOf("```");
            if (lastFence >= 0) {
                t = t.substring(0, lastFence);
            }
        }
        return t.trim();
    }
}
