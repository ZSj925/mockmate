package zsj.mockmate.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zsj.mockmate.pojo.Result;
import zsj.mockmate.pojo.vo.InterviewMessageVO;
import zsj.mockmate.pojo.vo.InterviewReplyVO;
import zsj.mockmate.pojo.vo.InterviewSessionVO;
import zsj.mockmate.service.InterviewService;

import java.util.List;

/**
 * 面试会话相关的接口。
 * <p>
 * 控制器只做三件事：接参数、参数校验、把结果包成 Result。
 * 会话解析（是哪一场、是不是你的、要不要新建）、调模型、消息落库，全在 InterviewService。
 */
@RestController
@RequestMapping("/interview")
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 对话输出（非流式，输出纯文本）
     *
     * @param q              问题，1~2000 字
     * @param conversationId 会话号，可空；第一次不传，后端会新建一场并把号返回给前端
     * @return 会话号 + 模型回答
     */
    @GetMapping("/chat")
    public Result<InterviewReplyVO> chat(@RequestParam
                                         @NotBlank(message = "问题不能为空")
                                         @Size(max = 2000, message = "问题不能超过 2000 字") String q,
                                         @RequestParam(required = false)
                                         @Size(max = 36, message = "Id大小不能超过36字符") String conversationId) {
        log.info("对话请求，conversationId={}", conversationId);
        return Result.success(interviewService.chat(q, conversationId));
    }

    /**
     * 我的会话列表（带首句预览和消息条数），顺手就是"当前用户有多少会话"
     */
    @GetMapping("/sessions")
    public Result<List<InterviewSessionVO>> sessions() {
        return Result.success(interviewService.listSessions());
    }

    /**
     * 删除一场会话，连同它的消息。别人的会话会被拒绝
     */
    @DeleteMapping("/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable Long id) {
        interviewService.deleteSession(id);
        return Result.success();
    }

    /**
     * 一场会话的全部历史消息（左侧点某一场时用来重绘聊天气泡）
     */
    @GetMapping("/sessions/{id}/messages")
    public Result<List<InterviewMessageVO>> history(@PathVariable Long id) {
        return Result.success(interviewService.history(id));
    }
}
