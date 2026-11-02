package zsj.mockmate.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewSessionEntity {
    private Long id;
    private Long userId;
    /** 这场面试依据的简历；null 表示不使用简历的通用面试 */
    private Long resumeId;
    private String jobType;
    private String status;          // ONGOING / FINISHED
    private Integer questionCount;
    private Integer currentRound;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime updatedAt;
}
