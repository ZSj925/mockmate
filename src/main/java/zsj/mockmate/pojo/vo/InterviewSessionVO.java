package zsj.mockmate.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表里的一行（前端左侧"我的会话"用）。
 * title 取这场会话里第一句用户说的话，做个预览。
 */
@Data
public class InterviewSessionVO {

    private Long id;
    private String title;
    private Integer messageCount;
    private Integer currentRound;
    private LocalDateTime startedAt;
}
