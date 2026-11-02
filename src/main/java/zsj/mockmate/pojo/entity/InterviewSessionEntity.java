package zsj.mockmate.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewSessionEntity {
    private Long id;
    private Long userId;
    private String jobType;
    private String status;          // ONGOING / FINISHED
    private Integer questionCount;
    private Integer currentRound;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime updatedAt;
}